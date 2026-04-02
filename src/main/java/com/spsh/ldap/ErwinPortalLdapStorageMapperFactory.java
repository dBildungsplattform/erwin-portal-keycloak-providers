package com.spsh.ldap;

import java.util.List;

import org.keycloak.component.ComponentModel;
import org.keycloak.provider.ProviderConfigProperty;
import org.keycloak.storage.ldap.LDAPStorageProvider;
import org.keycloak.storage.ldap.mappers.AbstractLDAPStorageMapper;
import org.keycloak.storage.ldap.mappers.AbstractLDAPStorageMapperFactory;

public class ErwinPortalLdapStorageMapperFactory extends AbstractLDAPStorageMapperFactory {

    @Override
    protected AbstractLDAPStorageMapper createMapper(ComponentModel mapperModel, LDAPStorageProvider federationProvider) {
        return new ErwinPortalLdapStorageMapper(mapperModel, federationProvider);
    }

    @Override
    public String getId() {
        return "erwin-portal-ldap-storage-mapper";
    }
    
    @Override
    public List<ProviderConfigProperty> getConfigProperties() {
        return ErwinLdapMapperConfig.CONFIG_PROPERTIES;
    }
    
}