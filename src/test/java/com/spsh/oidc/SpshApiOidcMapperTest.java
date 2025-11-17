package com.spsh.oidc;

import com.spsh.oidc.dto.FetchUrlResponse;
import com.spsh.util.ApiFetchHelper;
import com.spsh.util.JsonHelper;
import jakarta.ws.rs.InternalServerErrorException;
import jakarta.ws.rs.NotFoundException;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.keycloak.models.*;
import org.keycloak.protocol.oidc.mappers.OIDCAttributeMapperHelper;
import org.keycloak.provider.ProviderConfigProperty;
import org.keycloak.representations.IDToken;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.MockitoJUnitRunner;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.spsh.oidc.SpshApiOidcMapper.*;
import static org.junit.Assert.*;
import static org.keycloak.protocol.ProtocolMapperUtils.MULTIVALUED;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class SpshApiOidcMapperTest {

    // expose protected setClaim
    static class TestableSpshApiOidcMapper extends SpshApiOidcMapper {
        public void callSetClaim(IDToken token,
                                 ProtocolMapperModel mappingModel,
                                 UserSessionModel userSession,
                                 KeycloakSession keycloakSession,
                                 ClientSessionContext clientSessionCtx) {
            super.setClaim(token, mappingModel, userSession, keycloakSession, clientSessionCtx);
        }
    }

    private final TestableSpshApiOidcMapper mapper = new TestableSpshApiOidcMapper();

    @Mock
    ProtocolMapperModel mappingModel;
    @Mock
    UserSessionModel userSession;
    @Mock
    KeycloakSession keycloakSession;
    @Mock
    ClientSessionContext clientSessionCtx;
    @Mock
    AuthenticatedClientSessionModel clientSession;
    @Mock
    ClientModel clientModel;
    @Mock
    UserModel user;
    @Mock
    RoleModel roleModel;
    @Mock
    FetchUrlResponse fetchUrlResponse;

    private Map<String, String> baseConfig() {
        Map<String, String> config = new HashMap<>();
        config.put(FETCH_URL, "https://example.com/api");
        config.put(EXTRACT_JSON_PATH, "$.path");
        config.put(IGNORE_MISSING_PATH, "false");
        config.put(MULTIVALUED, "false");
        config.put(FAIL_MODE, "deny");
        config.put(TIMEOUT_MS, "1500");
        config.put(CACHE_TTL_SECONDS, "60");
        config.put(AUTH_HEADER_NAME, "api-key");
        return config;
    }

    private void setupCommonMocks(Map<String, String> config) {
        when(mappingModel.getConfig()).thenReturn(config);
        when(mappingModel.getId()).thenReturn("mapper-id");

        when(userSession.getUser()).thenReturn(user);
        when(user.getId()).thenReturn("user-123");
        when(user.getUsername()).thenReturn("john");

        when(clientSessionCtx.getClientSession()).thenReturn(clientSession);
        when(clientSession.getClient()).thenReturn(clientModel);
        when(clientModel.getName()).thenReturn("test-client");
        when(clientModel.getClientId()).thenReturn("test-client-id");
    }

    @Test
    public void getDisplayCategory_returnsTokenMapper() {
        assertEquals("Token Mapper", mapper.getDisplayCategory());
    }

    @Test
    public void getDisplayType_returnsCustomDisplay() {
        assertEquals("ErWIn Custom OIDC Api Mapper", mapper.getDisplayType());
    }

    @Test
    public void getHelpText_notNull() {
        assertNotNull(mapper.getHelpText());
    }

    @Test
    public void getId_returnsProviderId() {
        assertEquals(PROVIDER_ID, mapper.getId());
    }

    @Test
    public void getConfigProperties_notEmpty() {
        List<ProviderConfigProperty> props = mapper.getConfigProperties();
        assertFalse(props.isEmpty());
    }

    @Test
    public void setClaim_userNull_doesNothing() {
        Map<String, String> config = baseConfig();
        when(mappingModel.getConfig()).thenReturn(config);
        when(userSession.getUser()).thenReturn(null);

        mapper.callSetClaim(new IDToken(), mappingModel, userSession, keycloakSession, clientSessionCtx);
    }

    @Test(expected = NotFoundException.class)
    public void setClaim_fetchUrlNull_throwsNotFoundException() {
        Map<String, String> config = baseConfig();
        config.remove(FETCH_URL);
        setupCommonMocks(config);

        mapper.callSetClaim(new IDToken(), mappingModel, userSession, keycloakSession, clientSessionCtx);
    }

    @Test(expected = NotFoundException.class)
    public void setClaim_jsonPathNull_throwsNotFoundException() {
        Map<String, String> config = baseConfig();
        config.remove(EXTRACT_JSON_PATH);
        setupCommonMocks(config);

        mapper.callSetClaim(new IDToken(), mappingModel, userSession, keycloakSession, clientSessionCtx);
    }

    @Test(expected = NotFoundException.class)
    public void setClaim_userIdNull_throwsNotFoundException() {
        Map<String, String> config = baseConfig();
        setupCommonMocks(config);
        when(user.getId()).thenReturn(null);

        mapper.callSetClaim(new IDToken(), mappingModel, userSession, keycloakSession, clientSessionCtx);
    }

    @Test(expected = IllegalArgumentException.class)
    public void setClaim_clientNameBlank_throwsIllegalArgumentException() {
        Map<String, String> config = baseConfig();
        setupCommonMocks(config);
        when(clientModel.getName()).thenReturn("   ");

        mapper.callSetClaim(new IDToken(), mappingModel, userSession, keycloakSession, clientSessionCtx);
    }

    @Test
    public void setClaim_validCache_usesCachedNoBackendCall() {
        Map<String, String> config = baseConfig();
        setupCommonMocks(config);

        when(userSession.getNote("spsh_mapper_cache_value_mapper-id"))
                .thenReturn("{\"some\":\"json\"}");
        when(userSession.getNote("spsh_mapper_cache_ts_mapper-id"))
                .thenReturn(Long.toString(System.currentTimeMillis()));

        try (MockedStatic<JsonHelper> jsonHelperMock = mockStatic(JsonHelper.class);
             MockedStatic<ApiFetchHelper> apiFetchMock = mockStatic(ApiFetchHelper.class);
             MockedStatic<OIDCAttributeMapperHelper> oidcMapperMock = mockStatic(OIDCAttributeMapperHelper.class)) {

            jsonHelperMock.when(() -> JsonHelper.isPathExisting(anyString(), anyString()))
                    .thenReturn(true);
            jsonHelperMock.when(() -> JsonHelper.extractFromJson(anyString()))
                    .thenReturn(fetchUrlResponse);
            when(fetchUrlResponse.getMapToLmsRolle()).thenReturn("role-1");
            when(clientModel.getRole("role-1")).thenReturn(roleModel);
            when(user.hasRole(roleModel)).thenReturn(false);

            IDToken token = new IDToken();

            mapper.callSetClaim(token, mappingModel, userSession, keycloakSession, clientSessionCtx);

            apiFetchMock.verifyNoInteractions();

            oidcMapperMock.verify(() ->
                    OIDCAttributeMapperHelper.mapClaim(eq(token), eq(mappingModel), eq(fetchUrlResponse)));

            verify(user).grantRole(roleModel);
        }
    }

    @Test
    public void setClaim_noCache_callsBackend_assignsRole_mapsClaim() {
        Map<String, String> config = baseConfig();
        setupCommonMocks(config);

        when(userSession.getNote(anyString())).thenReturn(null);

        try (MockedStatic<JsonHelper> jsonHelperMock = mockStatic(JsonHelper.class);
             MockedStatic<ApiFetchHelper> apiFetchMock = mockStatic(ApiFetchHelper.class);
             MockedStatic<OIDCAttributeMapperHelper> oidcMapperMock = mockStatic(OIDCAttributeMapperHelper.class)) {

            jsonHelperMock.when(() -> JsonHelper.isPathExisting(anyString(), anyString()))
                    .thenReturn(true);
            jsonHelperMock.when(() -> JsonHelper.extractFromJson(anyString()))
                    .thenReturn(fetchUrlResponse);
            when(fetchUrlResponse.getMapToLmsRolle()).thenReturn("role-1");
            when(clientModel.getRole("role-1")).thenReturn(roleModel);
            when(user.hasRole(roleModel)).thenReturn(false);

            apiFetchMock.when(() -> ApiFetchHelper.fetchApiData(anyString(), anyString(), anyString(), anyString(), anyInt()))
                    .thenReturn("{\"ok\":true}");

            IDToken token = new IDToken();
            mapper.callSetClaim(token, mappingModel, userSession, keycloakSession, clientSessionCtx);

            apiFetchMock.verify(() -> ApiFetchHelper.fetchApiData(
                    eq("https://example.com/api"),
                    eq("user-123"),
                    eq("test-client"),
                    eq("api-key"),
                    eq(1500)));

            verify(user).grantRole(roleModel);

            oidcMapperMock.verify(() ->
                    OIDCAttributeMapperHelper.mapClaim(eq(token), eq(mappingModel), eq(fetchUrlResponse)));

            verify(userSession).setNote(eq("spsh_mapper_cache_value_mapper-id"), anyString());
            verify(userSession).setNote(eq("spsh_mapper_cache_ts_mapper-id"), anyString());
        }
    }

    @Test(expected = InternalServerErrorException.class)
    public void setClaim_ignoreMissingTrue_pathMissing_throwsInternalServerError() {
        Map<String, String> config = baseConfig();
        config.put(IGNORE_MISSING_PATH, "true");
        setupCommonMocks(config);

        when(userSession.getNote(anyString())).thenReturn(null);

        try (MockedStatic<JsonHelper> jsonHelperMock = mockStatic(JsonHelper.class);
             MockedStatic<ApiFetchHelper> apiFetchMock = mockStatic(ApiFetchHelper.class)) {

            jsonHelperMock.when(() -> JsonHelper.isPathExisting(anyString(), anyString()))
                    .thenReturn(false);

            apiFetchMock.when(() -> ApiFetchHelper.fetchApiData(anyString(), anyString(), anyString(), anyString(), anyInt()))
                    .thenReturn("{\"other\":\"json\"}");

            mapper.callSetClaim(new IDToken(), mappingModel, userSession, keycloakSession, clientSessionCtx);
        }
    }

    @Test
    public void setClaim_failModeAllowNoClaim_pathMissing_doesNotThrow() {
        Map<String, String> config = baseConfig();
        config.put(IGNORE_MISSING_PATH, "false");
        config.put(FAIL_MODE, "allowNoClaim");
        setupCommonMocks(config);

        when(userSession.getNote(anyString())).thenReturn(null);

        try (MockedStatic<JsonHelper> jsonHelperMock = mockStatic(JsonHelper.class);
             MockedStatic<ApiFetchHelper> apiFetchMock = mockStatic(ApiFetchHelper.class)) {

            jsonHelperMock.when(() -> JsonHelper.isPathExisting(anyString(), anyString()))
                    .thenReturn(false);

            apiFetchMock.when(() -> ApiFetchHelper.fetchApiData(anyString(), anyString(), anyString(), anyString(), anyInt()))
                    .thenReturn("{\"other\":\"json\"}");

            mapper.callSetClaim(new IDToken(), mappingModel, userSession, keycloakSession, clientSessionCtx);
        }
    }

    @Test
    public void setClaim_backendIOException_failModeAllowNoClaim_skipsClaim() {
        Map<String, String> config = baseConfig();
        config.put(FAIL_MODE, "allowNoClaim");
        setupCommonMocks(config);

        when(userSession.getNote(anyString())).thenReturn(null);

        try (MockedStatic<JsonHelper> jsonHelperMock = mockStatic(JsonHelper.class);
             MockedStatic<ApiFetchHelper> apiFetchMock = mockStatic(ApiFetchHelper.class)) {

            jsonHelperMock.when(() -> JsonHelper.isPathExisting(anyString(), anyString()))
                    .thenReturn(true);

            apiFetchMock.when(() -> ApiFetchHelper.fetchApiData(anyString(), anyString(), anyString(), anyString(), anyInt()))
                    .thenThrow(new IOException("backend down"));

            mapper.callSetClaim(new IDToken(), mappingModel, userSession, keycloakSession, clientSessionCtx);
        }
    }

    @Test
    public void setClaim_roleNameBlank_skipsGrant() {
        Map<String, String> config = baseConfig();
        setupCommonMocks(config);
        when(userSession.getNote(anyString())).thenReturn(null);

        try (MockedStatic<JsonHelper> jsonHelperMock = mockStatic(JsonHelper.class);
             MockedStatic<ApiFetchHelper> apiFetchMock = mockStatic(ApiFetchHelper.class)) {

            jsonHelperMock.when(() -> JsonHelper.isPathExisting(anyString(), anyString()))
                    .thenReturn(true);
            jsonHelperMock.when(() -> JsonHelper.extractFromJson(anyString()))
                    .thenReturn(fetchUrlResponse);
            when(fetchUrlResponse.getMapToLmsRolle()).thenReturn("   ");

            apiFetchMock.when(() -> ApiFetchHelper.fetchApiData(anyString(), anyString(), anyString(), anyString(), anyInt()))
                    .thenReturn("{\"ok\":true}");

            mapper.callSetClaim(new IDToken(), mappingModel, userSession, keycloakSession, clientSessionCtx);

            verify(user, never()).grantRole(any());
        }
    }

    @Test
    public void setClaim_roleNotFound_skipsGrant() {
        Map<String, String> config = baseConfig();
        setupCommonMocks(config);
        when(userSession.getNote(anyString())).thenReturn(null);

        try (MockedStatic<JsonHelper> jsonHelperMock = mockStatic(JsonHelper.class);
             MockedStatic<ApiFetchHelper> apiFetchMock = mockStatic(ApiFetchHelper.class)) {

            jsonHelperMock.when(() -> JsonHelper.isPathExisting(anyString(), anyString()))
                    .thenReturn(true);
            jsonHelperMock.when(() -> JsonHelper.extractFromJson(anyString()))
                    .thenReturn(fetchUrlResponse);
            when(fetchUrlResponse.getMapToLmsRolle()).thenReturn("role-missing");
            when(clientModel.getRole("role-missing")).thenReturn(null);

            apiFetchMock.when(() -> ApiFetchHelper.fetchApiData(anyString(), anyString(), anyString(), anyString(), anyInt()))
                    .thenReturn("{\"ok\":true}");

            mapper.callSetClaim(new IDToken(), mappingModel, userSession, keycloakSession, clientSessionCtx);

            verify(user, never()).grantRole(any());
        }
    }

    @Test
    public void setClaim_userAlreadyHasRole_doesNotGrantAgain() {
        Map<String, String> config = baseConfig();
        setupCommonMocks(config);
        when(userSession.getNote(anyString())).thenReturn(null);

        try (MockedStatic<JsonHelper> jsonHelperMock = mockStatic(JsonHelper.class);
             MockedStatic<ApiFetchHelper> apiFetchMock = mockStatic(ApiFetchHelper.class)) {

            jsonHelperMock.when(() -> JsonHelper.isPathExisting(anyString(), anyString()))
                    .thenReturn(true);
            jsonHelperMock.when(() -> JsonHelper.extractFromJson(anyString()))
                    .thenReturn(fetchUrlResponse);
            when(fetchUrlResponse.getMapToLmsRolle()).thenReturn("role-1");
            when(clientModel.getRole("role-1")).thenReturn(roleModel);
            when(user.hasRole(roleModel)).thenReturn(true);

            apiFetchMock.when(() -> ApiFetchHelper.fetchApiData(anyString(), anyString(), anyString(), anyString(), anyInt()))
                    .thenReturn("{\"ok\":true}");

            mapper.callSetClaim(new IDToken(), mappingModel, userSession, keycloakSession, clientSessionCtx);

            verify(user, never()).grantRole(any());
        }
    }
}
