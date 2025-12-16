package com.spsh.ldap;

import java.util.List;

import org.keycloak.component.ComponentModel;
import org.keycloak.models.KeycloakSession;
import org.keycloak.provider.ProviderConfigProperty;
import org.keycloak.storage.ldap.mappers.LDAPStorageMapperFactory;

public class ErwinPortalLdapStorageMapperFactory implements LDAPStorageMapperFactory<ErwinPortalLdapStorageMapper> {

    @Override
    public ErwinPortalLdapStorageMapper create(KeycloakSession session, ComponentModel model) {
        return new ErwinPortalLdapStorageMapper(session, model);
    }

    @Override
    public String getId() {
        return "erwin-portal-ldap-storage-mapper";
    }
    
    @Override
    public List<ProviderConfigProperty> getConfigProperties() {
        return ErwinPortalLdapStorageMapper.CONFIG_PROPERTIES;
    }
    
}