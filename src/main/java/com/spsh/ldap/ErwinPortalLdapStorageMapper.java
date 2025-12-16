package com.spsh.ldap;

import java.io.IOException;
import java.net.URI;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import javax.naming.AuthenticationException;

import org.jboss.logging.Logger;
import org.keycloak.common.util.MultivaluedHashMap;
import org.keycloak.component.ComponentModel;
import org.keycloak.models.GroupModel;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.RealmModel;
import org.keycloak.models.RoleModel;
import org.keycloak.models.UserModel;
import org.keycloak.provider.ProviderConfigProperty;
import org.keycloak.provider.ProviderConfigurationBuilder;
import org.keycloak.storage.ldap.LDAPStorageProvider;
import org.keycloak.storage.ldap.idm.model.LDAPObject;
import org.keycloak.storage.ldap.idm.query.internal.LDAPQuery;
import org.keycloak.storage.ldap.mappers.LDAPStorageMapper;
import org.keycloak.storage.user.SynchronizationResult;

import com.spsh.util.ApiFetchHelper;

public class ErwinPortalLdapStorageMapper implements LDAPStorageMapper {

    public static List<ProviderConfigProperty> CONFIG_PROPERTIES;

    private static final Logger LOGGER = Logger.getLogger(ErwinPortalLdapStorageMapper.class);

    public static final String CONFIG_KEY_NEW_LDAP_USER_URL = "NEW_LDAP_USER_URL";
    public static final String CONFIG_KEY_USERNAME_ATTR_NAME = "USERNAME_ATTR_NAME";
    public static final String CONFIG_KEY_EMAIL_ATTR_NAME = "EMAIL_ATTR_NAME";
    public static final String CONFIG_KEY_FIRSTNAME_ATTR_NAME = "FIRSTNAME_ATTR_NAME";
    public static final String CONFIG_KEY_LASTNAME_ATTR_NAME = "LASTNAME_ATTR_NAME";

    static {
        CONFIG_PROPERTIES = ProviderConfigurationBuilder.create()
                // Url to send a new user
                .property()
                .name(CONFIG_KEY_NEW_LDAP_USER_URL)
                .label("New LDAP user url")
                .type(ProviderConfigProperty.STRING_TYPE)
                .required(true)
                .add()

                // LDAP attribute for the username
                .property()
                .name(CONFIG_KEY_USERNAME_ATTR_NAME)
                .label("Username LDAP Attribute")
                .type(ProviderConfigProperty.STRING_TYPE)
                .defaultValue("uid")
                .add()

                // LDAP attribute for the email
                .property()
                .name(CONFIG_KEY_EMAIL_ATTR_NAME)
                .label("Email LDAP Attribute")
                .type(ProviderConfigProperty.STRING_TYPE)
                .defaultValue("mail")
                .add()

                // LDAP attribute for the first name
                .property()
                .name(CONFIG_KEY_FIRSTNAME_ATTR_NAME)
                .label("First name LDAP Attribute")
                .type(ProviderConfigProperty.STRING_TYPE)
                .defaultValue("givenName")
                .add()

                // LDAP attribute for the last name
                .property()
                .name(CONFIG_KEY_LASTNAME_ATTR_NAME)
                .label("Last name LDAP Attribute")
                .type(ProviderConfigProperty.STRING_TYPE)
                .defaultValue("sn")
                .add()

                .build();
    }

    private final MultivaluedHashMap<String, String> config;

    public ErwinPortalLdapStorageMapper(KeycloakSession session, ComponentModel model) {
        this.config = model.getConfig();
    }

    @Override
    public void onImportUserFromLDAP(LDAPObject ldapUser, UserModel user, RealmModel realm, boolean isCreate) {
        final var url = config.getFirst(CONFIG_KEY_NEW_LDAP_USER_URL);
        if (url == null) {
            LOGGER.warnf("Config variable %s is not set or is empty.", CONFIG_KEY_NEW_LDAP_USER_URL);
            return;
        }

        final var uri = URI.create(url);
        final var keycloakUserId = user.getId();
        final var userName = ldapUser.getAttributeAsString(config.getFirst(CONFIG_KEY_USERNAME_ATTR_NAME));
        final var email = ldapUser.getAttributeAsString(config.getFirst(CONFIG_KEY_EMAIL_ATTR_NAME));
        final var firstName = ldapUser.getAttributeAsString(config.getFirst(CONFIG_KEY_FIRSTNAME_ATTR_NAME));
        final var lastName = ldapUser.getAttributeAsString(config.getFirst(CONFIG_KEY_LASTNAME_ATTR_NAME));
        final var ldapDn = ldapUser.getDn().toString();
        final var ldapId = ldapUser.getUuid();

        try {
            ApiFetchHelper.sendLdapUserData(uri, keycloakUserId, userName, email, firstName, lastName,
                    ldapDn, ldapId);
        } catch (final IOException e) {
            LOGGER.error("IOException occurred while sending user to ErWIn-Portal", e);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    // unimportant
    @Override
    public void close() {

    }

    @Override
    public void onRegisterUserToLDAP(LDAPObject ldapUser, UserModel localUser, RealmModel realm) {

    }

    @Override
    public LDAPStorageProvider getLdapProvider() {
        return null;
    }

    @Override
    public boolean onAuthenticationFailure(LDAPObject ldapUser, UserModel user, AuthenticationException ldapException,
            RealmModel realm) {
        return false;
    }

    @Override
    public void beforeLDAPQuery(LDAPQuery query) {

    }

    @Override
    public UserModel proxy(LDAPObject ldapUser, UserModel delegate, RealmModel realm) {
        return delegate;
    }

    @Override
    public List<UserModel> getRoleMembers(RealmModel realm, RoleModel role, int firstResult, int maxResults) {
        return null;
    }

    @Override
    public List<UserModel> getGroupMembers(RealmModel realm, GroupModel group, int firstResult, int maxResults) {
        return null;
    }

    @Override
    public SynchronizationResult syncDataFromFederationProviderToKeycloak(RealmModel realm) {
        return new SynchronizationResult();
    }

    @Override
    public SynchronizationResult syncDataFromKeycloakToFederationProvider(RealmModel realm) {
        return new SynchronizationResult();
    }

    @Override
    public Set<String> mandatoryAttributeNames() {
        return null;
    }

    @Override
    public Set<String> getUserAttributes() {
        return Collections.emptySet();
    }
}