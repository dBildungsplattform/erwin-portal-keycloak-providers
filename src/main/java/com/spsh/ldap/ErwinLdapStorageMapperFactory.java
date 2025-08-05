package com.spsh.ldap;

import org.keycloak.component.ComponentModel;
import org.keycloak.models.KeycloakSession;
import org.keycloak.storage.ldap.mappers.LDAPStorageMapperFactory;

public class ErwinLdapStorageMapperFactory implements LDAPStorageMapperFactory<ErwinLdapStorageMapper> {

    @Override
    public ErwinLdapStorageMapper create(KeycloakSession session, ComponentModel model) {
        return new ErwinLdapStorageMapper(session, model);
    }

    @Override
    public String getId() {
        return "erwin-portal-ldap-storage-mapper";
    }
    
}
