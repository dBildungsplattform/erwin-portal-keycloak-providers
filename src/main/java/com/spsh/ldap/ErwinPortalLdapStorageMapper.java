package com.spsh.ldap;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpRequest.BodyPublishers;
import java.net.http.HttpResponse.BodyHandlers;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import javax.naming.AuthenticationException;

import org.jboss.logging.Logger;
import org.keycloak.component.ComponentModel;
import org.keycloak.models.GroupModel;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.RealmModel;
import org.keycloak.models.RoleModel;
import org.keycloak.models.UserModel;
import org.keycloak.storage.ldap.LDAPStorageProvider;
import org.keycloak.storage.ldap.idm.model.LDAPObject;
import org.keycloak.storage.ldap.idm.query.internal.LDAPQuery;
import org.keycloak.storage.ldap.mappers.LDAPStorageMapper;
import org.keycloak.storage.user.SynchronizationResult;

import com.spsh.util.ApiFetchHelper;

public class ErwinPortalLdapStorageMapper implements LDAPStorageMapper {

    // Maybe necessary later
    private final KeycloakSession session;
    private final ComponentModel model;

    private static final Logger LOGGER = Logger.getLogger(ErwinPortalLdapStorageMapper.class);

    private static final URI LDAP_USER_ENDPOINT = URI.create("http://localhost:9090/api/keycloakinternal/newldapuser");

    public ErwinPortalLdapStorageMapper(KeycloakSession session, ComponentModel model) {
        this.session = session;
        this.model = model;
    }

    @Override
    public void onImportUserFromLDAP(LDAPObject ldapUser, UserModel user, RealmModel realm, boolean isCreate) {
        final var keycloakUserId = user.getId();
        final var userName = user.getUsername();
        final var email = user.getEmail();
        final var firstName = user.getFirstName();
        final var lastName = user.getLastName();
        final var ldapDn = ldapUser.getDn().toString();
        final var ldapId = ldapUser.getUuid();

        try {
            ApiFetchHelper.sendLdapUserData(LDAP_USER_ENDPOINT, keycloakUserId, userName, email, firstName, lastName, ldapDn, ldapId);
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