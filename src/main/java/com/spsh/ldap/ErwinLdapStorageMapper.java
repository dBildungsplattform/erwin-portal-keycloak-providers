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

public class ErwinLdapStorageMapper implements LDAPStorageMapper {

    private KeycloakSession session;

    private ComponentModel model;

    private static final Logger log = Logger.getLogger(ErwinLdapStorageMapper.class);

    public ErwinLdapStorageMapper(KeycloakSession session, ComponentModel model) {
        this.session = session;
        this.model = model;
    }

    @Override
    public void onImportUserFromLDAP(LDAPObject ldapUser, UserModel user, RealmModel realm, boolean isCreate) {
        String headerMessage = isCreate ? "New User Imported from LDAP: " : "Existing User Updated From LDAP: ";
        String message = headerMessage + user.getId() + user.getUsername();
        log.info(message);
        try {
            HttpClient.newHttpClient().send(HttpRequest.newBuilder(URI.create("http://host.docker.internal:8888/ldap")).POST(BodyPublishers.ofString(message)).build(),
                    BodyHandlers.discarding());
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
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
