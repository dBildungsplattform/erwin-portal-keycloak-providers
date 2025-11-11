package com.spsh.oidc;

import java.util.*;
import java.util.concurrent.Callable;
import java.util.stream.Collectors;

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

import jakarta.ws.rs.InternalServerErrorException;

import static java.lang.Integer.parseInt;
import static org.keycloak.protocol.ProtocolMapperUtils.*;

public class SpshApiOidcMapper extends AbstractOIDCProtocolMapper implements OIDCAccessTokenMapper, OIDCIDTokenMapper, UserInfoTokenMapper {


    public static final String ENV_KEY_INTERNAL_COMMUNICATION_API_KEY = "INTERNAL_COMMUNICATION_API_KEY";
    public static final String PROVIDER_ID = "spsh-custom-oidc-api-mapper";
    public static final String FETCH_URL = "fetchUrl";
    public static final String EXTRACT_JSON_PATH = "extractJsonPath";
    public static final String IGNORE_MISSING_PATH = "ignoreMissingPath";
    private static final List<ProviderConfigProperty> configProperties = new ArrayList<>();
    private static final Logger LOGGER = Logger.getLogger(SpshApiOidcMapper.class);


    public static final String TIMEOUT_MS = "timeoutMs";
    public static final String CACHE_TTL_SECONDS = "cacheTtlSeconds";
    public static final String FAIL_MODE = "failMode";
    public static final String AUTH_HEADER_NAME = "authHeaderName";

    static {
        OIDCAttributeMapperHelper.addTokenClaimNameConfig(configProperties);
        OIDCAttributeMapperHelper.addIncludeInTokensConfig(configProperties, SpshApiOidcMapper.class);
        OIDCAttributeMapperHelper.addJsonTypeConfig(configProperties);

        ProviderConfigProperty multivaluedProperty = new ProviderConfigProperty();
        multivaluedProperty.setName(MULTIVALUED);
        multivaluedProperty.setLabel(MULTIVALUED_LABEL);
        multivaluedProperty.setType(ProviderConfigProperty.BOOLEAN_TYPE);
        multivaluedProperty.setHelpText(MULTIVALUED_HELP_TEXT);
        configProperties.add(multivaluedProperty);

        ProviderConfigProperty fetchUrlProperty = new ProviderConfigProperty();
        fetchUrlProperty.setName(FETCH_URL);
        fetchUrlProperty.setLabel("SPSH Fetch Url");
        fetchUrlProperty.setType(ProviderConfigProperty.STRING_TYPE);
        fetchUrlProperty.setHelpText("The URL to fetch data from the SPSH Backend.");
        configProperties.add(fetchUrlProperty);

        ProviderConfigProperty extractPathProperty = new ProviderConfigProperty();
        extractPathProperty.setName(EXTRACT_JSON_PATH);
        extractPathProperty.setLabel("SPSH Extract Json Path");
        extractPathProperty.setType(ProviderConfigProperty.STRING_TYPE);
        extractPathProperty.setHelpText("The JSON path to extract data from the API response.");
        configProperties.add(extractPathProperty);

        ProviderConfigProperty ignoreMissingPathProperty = new ProviderConfigProperty();
        ignoreMissingPathProperty.setName(IGNORE_MISSING_PATH);
        ignoreMissingPathProperty.setLabel("SPSH Ignore Missing Path");
        ignoreMissingPathProperty.setType(ProviderConfigProperty.BOOLEAN_TYPE);
        ignoreMissingPathProperty.setHelpText("If JSON Path cannot be found in response received from Backend, do not throw an error, just ignore it.");
        configProperties.add(ignoreMissingPathProperty);


    }

    @Override
    public String getDisplayCategory() {
        return "Token Mapper";
    }

    @Override
    public String getDisplayType() {
        return "SPSH Custom OIDC Api Mapper";
    }

    @Override
    public String getHelpText() {
        return "The mapper calls the provided SPSH fetch url, extracts the provided JsonPath from the api response and maps the result if not null to the claim";
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
    protected void setClaim(IDToken token,
                            ProtocolMapperModel mappingModel,
                            UserSessionModel userSession,
                            KeycloakSession keycloakSession,
                            ClientSessionContext clientSessionCtx) {

        final Map<String, String> cfg = mappingModel.getConfig();

        final String fetchUrl        = cfg.get(FETCH_URL);
        final String jsonPath        = cfg.get(EXTRACT_JSON_PATH);
        final boolean ignoreMissing  = Boolean.parseBoolean(cfg.getOrDefault(IGNORE_MISSING_PATH, "false"));
        final boolean multivalued    = Boolean.parseBoolean(cfg.getOrDefault(MULTIVALUED, "false"));
        final String  failMode       = cfg.getOrDefault(FAIL_MODE, "deny");
        final int     timeoutMs      = parseInt(cfg.getOrDefault(TIMEOUT_MS, "1500"));
        final int     cacheTtlSec    = parseInt(cfg.getOrDefault(CACHE_TTL_SECONDS, "60"));
        final String  headerName     = cfg.getOrDefault(AUTH_HEADER_NAME, "api-key");


        final var user = (userSession != null) ? userSession.getUser() : null;
        if (user == null) return;
        final String userId = user.getId();
        if (isBlank(fetchUrl) || isBlank(jsonPath) || isBlank(userId)) {
            LOGGER.warn("SpshApiOidcMapper: missing fetchUrl/extractJsonPath/userId; skipping.");
            return;
        }

        final var kcClientSession = clientSessionCtx.getClientSession();
        final var clientModel     = kcClientSession.getClient();
        final String clientId     = safe(clientModel::getClientId);
        String clientName         = safe(clientModel::getName);

        if (isBlank(clientName)) clientName = clientId;

        final String cacheValKey = "spsh_mapper_cache_value_" + mappingModel.getId();
        final String cacheTsKey  = "spsh_mapper_cache_ts_" + mappingModel.getId();

        try {
            String cached = userSession.getNote(cacheValKey);
            String ts = userSession.getNote(cacheTsKey);
            long now = System.currentTimeMillis();
            if (cached != null && ts != null) {
                try {
                    long fetchedAt = Long.parseLong(ts);
                    if ((now - fetchedAt) <= cacheTtlSec * 1000L) {
                        Object v = extractValueOrHandleMissing(cached, jsonPath, ignoreMissing, failMode);
                        if (v != null || ignoreMissing) {
                            mapValue(token, mappingModel, v, multivalued);
                            return;
                        }
                    }
                } catch (Exception e) {
                    LOGGER.debug("Cache parse/expiry failed; fetching fresh.", e);
                }
            }

            String json = ApiFetchHelper.fetchApiData(
                    fetchUrl, userId, clientId, clientName, headerName, timeoutMs
            );
            Object value = extractValueOrHandleMissing(json, jsonPath, ignoreMissing, failMode);

            if (value == null && ignoreMissing) return;
            assignRoleToUser(user, clientSessionCtx, value);
            mapValue(token, mappingModel, value, multivalued);

            try {
                userSession.setNote(cacheValKey, json);
                userSession.setNote(cacheTsKey, Long.toString(System.currentTimeMillis()));
            } catch (Exception e) {
                LOGGER.debug("Failed to write session cache.", e);
            }

        } catch (Exception e) {
            if ("deny".equalsIgnoreCase(failMode)) {
                LOGGER.error("SpshApiOidcMapper: backend fetch/mapping failed; denying token issuance.", e);
                throw (e instanceof InternalServerErrorException)
                        ? (InternalServerErrorException) e
                        : new InternalServerErrorException("SpshApiOidcMapper failed", e);
            } else {
                LOGGER.warn("SpshApiOidcMapper: backend fetch/mapping failed; skipping claim per failMode=allowNoClaim.", e);
            }
        }
    }

    /* Helper classes for data manipulation */

    private static boolean isBlank(String s) { return s == null || s.isBlank(); }

    private static <T> T safe(Callable<T> c) {
        try { return c.call(); } catch (Exception e) { return null; }
    }

    private static Object extractValueOrHandleMissing(String json, String jsonPath, boolean ignoreMissing, String failMode) {
        boolean exists = ApiFetchHelper.isPathExisting(json, jsonPath);
        if (!exists) {
            if (ignoreMissing) return null;
            if ("deny".equalsIgnoreCase(failMode)) {
                throw new InternalServerErrorException("JSONPath " + jsonPath + " not found in backend response.");
            }
            LOGGER.warnf("JSONPath %s not found; skipping per failMode=allowNoClaim.", jsonPath);
            return null;
        }
        Object jsonValue = ApiFetchHelper.extractFromJson(json, jsonPath);
        if (jsonValue instanceof Collection<?> col) {
            jsonValue = col.stream().filter(Objects::nonNull).collect(Collectors.toList());
        }
        return jsonValue;
    }

    private static void mapValue(IDToken token, ProtocolMapperModel model, Object value, boolean multivalued) {
        if (value == null) return;
        Object mapped = value;
        if (multivalued && !(value instanceof Collection<?>)) mapped = List.of(value);
        if (!multivalued && (value instanceof Collection<?> col)) {
            mapped = col.stream().filter(Objects::nonNull).findFirst().orElse(null);
            if (mapped == null) return;
        }

        OIDCAttributeMapperHelper.mapClaim(token, model, mapped);
    }


    private static void assignRoleToUser(UserModel user,
                                         ClientSessionContext clientCtx,
                                         Object value) {
        if (value == null) return;

        String roleName = value.toString().trim();
        if (roleName.isEmpty()) return;

        ClientModel client = clientCtx.getClientSession().getClient();
        RoleModel role = client.getRole(roleName);

        if (role == null) {
            LOGGER.warnf("Role '%s' not found in client '%s'; skipping.", roleName, client.getClientId());
            return;
        }

        if (!user.hasRole(role)) {
            user.grantRole(role);
            LOGGER.infof("Granted role '%s' to user '%s'.", roleName, user.getUsername());
        }
    }

}