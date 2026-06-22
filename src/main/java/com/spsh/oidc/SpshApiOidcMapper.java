package com.spsh.oidc;

import java.util.*;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.jboss.logging.Logger;
import org.keycloak.models.*;
import org.keycloak.protocol.oidc.mappers.AbstractOIDCProtocolMapper;
import org.keycloak.protocol.oidc.mappers.OIDCAccessTokenMapper;
import org.keycloak.protocol.oidc.mappers.OIDCAttributeMapperHelper;
import org.keycloak.protocol.oidc.mappers.OIDCIDTokenMapper;
import org.keycloak.protocol.oidc.mappers.UserInfoTokenMapper;
import org.keycloak.provider.ProviderConfigProperty;
import org.keycloak.representations.IDToken;

import com.spsh.util.ApiFetchHelper;

import static java.lang.Integer.parseInt;

public class SpshApiOidcMapper extends AbstractOIDCProtocolMapper implements OIDCAccessTokenMapper, OIDCIDTokenMapper, UserInfoTokenMapper {

    private static final Logger LOGGER = Logger.getLogger(SpshApiOidcMapper.class);

    public static final String PROVIDER_ID = "spsh-custom-oidc-api-mapper";

    private static final List<ProviderConfigProperty> configProperties = new ArrayList<>();

    public static final String FETCH_URL = "fetchUrl";
    public static final String TIMEOUT_MS = "timeoutMs";
    public static final String CACHE_TTL_SECONDS = "cacheTtlSeconds";

    static {
        OIDCAttributeMapperHelper.addTokenClaimNameConfig(configProperties);
        OIDCAttributeMapperHelper.addIncludeInTokensConfig(configProperties, SpshApiOidcMapper.class);
        OIDCAttributeMapperHelper.addJsonTypeConfig(configProperties);

        ProviderConfigProperty fetchUrlProperty = new ProviderConfigProperty();
        fetchUrlProperty.setName(FETCH_URL);
        fetchUrlProperty.setLabel("Erwin Fetch Url");
        fetchUrlProperty.setType(ProviderConfigProperty.STRING_TYPE);
        fetchUrlProperty.setHelpText("The URL to fetch data from the SPSH Backend.");
        configProperties.add(fetchUrlProperty);

        ProviderConfigProperty timeoutMsProperty = new ProviderConfigProperty();
        timeoutMsProperty.setName(TIMEOUT_MS);
        timeoutMsProperty.setLabel("Fetch timeout ms");
        timeoutMsProperty.setType(ProviderConfigProperty.INTEGER_TYPE);
        timeoutMsProperty.setHelpText("The fetch timeout in milliseconds");
        timeoutMsProperty.setDefaultValue("1500");
        configProperties.add(timeoutMsProperty);

        ProviderConfigProperty cacheTtlProperty = new ProviderConfigProperty();
        cacheTtlProperty.setName(CACHE_TTL_SECONDS);
        cacheTtlProperty.setLabel("Cache TTL seconds");
        cacheTtlProperty.setType(ProviderConfigProperty.INTEGER_TYPE);
        cacheTtlProperty.setHelpText("The cache lifetime in seconds");
        cacheTtlProperty.setDefaultValue("60");
        configProperties.add(cacheTtlProperty);
    }

    @Override
    public String getDisplayCategory() {
        return "Token Mapper";
    }

    @Override
    public String getDisplayType() {
        return "ErWIn Custom OIDC Api Mapper";
    }

    @Override
    public String getHelpText() {
        return "The mapper calls the provided ErWIn-Portal fetch url, extracts the provided JsonPath from the api response and maps the result if not null to the claim";
    }

    @Override
    public List<ProviderConfigProperty> getConfigProperties() {
        return configProperties;
    }

    @Override
    public String getId() {
        return PROVIDER_ID;
    }

    @Override
    protected void setClaim(IDToken token, ProtocolMapperModel mappingModel, UserSessionModel userSession, KeycloakSession keycloakSession, ClientSessionContext clientSessionCtx) {
        final var config = mappingModel.getConfig();

        final var fetchUrl = config.get(FETCH_URL);
        if (fetchUrl == null) {
            LOGGER.warn("SpshApiOidcMapper: fetchUrl is null. No data will be fetched, extracted and mapped.");
            throw new IllegalArgumentException("SpshApiOidcMapper: fetchUrl is null. No data will be fetched, extracted and mapped.");
        }

        final int timeoutMs = parseInt(config.getOrDefault(TIMEOUT_MS, "1500"));
        final int cacheTtlSec = parseInt(config.getOrDefault(CACHE_TTL_SECONDS, "60"));

        final UserModel user = (userSession != null) ? userSession.getUser() : null;
        if (user == null) {
            return;
        }

        final var userId = user.getId();
        if (userId == null) {
            LOGGER.warn("SpshApiOidcMapper: userId is null. No data will be fetched, extracted and mapped.");
            throw new IllegalArgumentException("SpshApiOidcMapper: userId is null. No data will be fetched, extracted and mapped.");
        }
        LOGGER.info(String.format("Setting claims via custom SpshApiOidcMapper for userSub: %s", userId));
        LOGGER.debug(String.format("Using fetchUrl: %s", fetchUrl));

        final var cacheValKey = "spsh_mapper_cache_value_" + mappingModel.getId();
        final var cacheTsKey = "spsh_mapper_cache_ts_" + mappingModel.getId();

        try {
            final var cached = getCachedData(userSession, cacheTtlSec, cacheValKey, cacheTsKey);

            final String dataToMap;
            if (cached == null) {
                dataToMap = ApiFetchHelper.fetchApiData(fetchUrl, userId, timeoutMs);
                writeCacheInUserSession(userSession, dataToMap, cacheValKey, cacheTsKey);
            } else {
                dataToMap = cached;
            }

            mapClaimsToToken(token, dataToMap);
        } catch (Exception e) {
            LOGGER.error("SpshApiOidcMapper: backend fetch/mapping failed; denying token issuance.", e);
            throw new UnsupportedOperationException("Token Claim Mapping failed", e);
        }
    }

    private String getCachedData(final UserSessionModel userSession, final int cacheTtlSec, final String cacheValKey, final String cacheTsKey) {
        LOGGER.debug("retrieving info from cache if valid");

        final var cached = userSession.getNote(cacheValKey);
        final var ts = userSession.getNote(cacheTsKey);
        final var now = System.currentTimeMillis();

        if (cached != null && ts != null) {
            LOGGER.debug("cache found, testing validity");

            final var fetchedAt = Long.parseLong(ts);
            if ((now - fetchedAt) <= cacheTtlSec * 1000L) {
                LOGGER.debug("cache valid, returning");

                return cached;
            }
        }

        LOGGER.debug("no cache found");

        return null;
    }

    private void writeCacheInUserSession(final UserSessionModel userSession, final String json, final String cacheValKey, final String cacheTsKey) {
        try {
            userSession.setNote(cacheValKey, json);
            userSession.setNote(cacheTsKey, Long.toString(System.currentTimeMillis()));
        } catch (Exception e) {
            LOGGER.debug("Failed to write session cache.", e);
        }
    }

    private static void mapClaimsToToken(final IDToken token, final String json) {
        if (json == null) {
            return;
        }

        try {
            final var jsonObj = new ObjectMapper().readTree(json);

            final var person = jsonObj.get("personData");
            final var schule = jsonObj.get("schuleData");
            final var klassen = jsonObj.get("klasseData");

            token.setOtherClaims("person", person);
            token.setOtherClaims("schule", schule);
            token.setOtherClaims("klassen", klassen);
        } catch (JsonProcessingException e) {
            throw new IllegalArgumentException("Can't parse json from token data endpoint", e);
        }
    }
}