package com.spsh.ldap;

import com.spsh.ldap.dto.ErwinRollenArt;
import com.spsh.ldap.dto.KlasseData;
import com.spsh.ldap.dto.LdapImportData;
import com.spsh.ldap.dto.PersonData;
import com.spsh.ldap.dto.SchuleData;
import org.jboss.logging.Logger;
import org.keycloak.common.util.MultivaluedHashMap;
import org.keycloak.component.ComponentModel;
import org.keycloak.models.GroupProvider;
import org.keycloak.models.RealmModel;
import org.keycloak.models.RoleProvider;
import org.keycloak.models.UserModel;
import org.keycloak.storage.ldap.LDAPStorageProvider;
import org.keycloak.storage.ldap.idm.model.LDAPObject;
import org.keycloak.storage.ldap.idm.query.internal.LDAPQuery;
import org.keycloak.storage.ldap.idm.query.internal.LDAPQueryConditionsBuilder;
import org.keycloak.storage.ldap.mappers.AbstractLDAPStorageMapper;

import javax.naming.directory.SearchControls;
import javax.naming.ldap.Rdn;
import java.io.IOException;
import java.net.URI;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;

import static com.spsh.ldap.ErwinLdapMapperConfig.*;

public class ErwinPortalLdapStorageMapper extends AbstractLDAPStorageMapper {

    private static final Logger LOGGER = Logger.getLogger(ErwinPortalLdapStorageMapper.class);

    private static final LDAPQueryConditionsBuilder LDAP_QUERY_CONDITIONS_BUILDER = new LDAPQueryConditionsBuilder();

    private static final DateTimeFormatter FORMATTER_STRING = DateTimeFormatter.ofPattern("yyyy'-'MM'-'dd");

    private static final DateTimeFormatter FORMATTER_GENERALIZED_TIME = DateTimeFormatter.ofPattern("yyyyMMddHHmmss'Z'");

    private final MultivaluedHashMap<String, String> config;

    private final LDAPStorageProvider ldapStorageProvider;

    private final GroupProvider groupProvider;

    private final RoleProvider roleProvider;

    public ErwinPortalLdapStorageMapper(ComponentModel mapperModel, LDAPStorageProvider ldapProvider) {
        super(mapperModel, ldapProvider);
        this.config = mapperModel.getConfig();
        this.ldapStorageProvider = ldapProvider;
        this.groupProvider = ldapProvider.getSession().getProvider(GroupProvider.class);
        this.roleProvider = ldapProvider.getSession().getProvider(RoleProvider.class);
    }

    @Override
    public void onImportUserFromLDAP(LDAPObject ldapUser, UserModel user, RealmModel realm, boolean isCreate) {
        final var url = config.getFirst(CONFIG_KEY_SERVER_NEW_LDAP_USER_URL);
        if (url == null) {
            LOGGER.warnf("Config variable %s is not set or is empty.", CONFIG_KEY_SERVER_NEW_LDAP_USER_URL);
            return;
        }

        final var uri = URI.create(url);

        final var person = extractPersonData(user, ldapUser);
        final var schule = extractSchuleData();
        final var klassen = extractKlassenData(ldapUser.getDn().toString());
        final var rolle = extractRolle(ldapUser);
        final var ldapImportData = new LdapImportData(person, schule, klassen, rolle);

        applyImportDataToKeycloakUser(user, realm, ldapImportData);

        try {
            LdapUserRequestHelper.sendLdapUserData(uri, ldapImportData);
        } catch (final IOException e) {
            LOGGER.error("IOException occurred while sending user to ErWIn-Portal", e);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    private void applyImportDataToKeycloakUser(UserModel user, RealmModel realm, LdapImportData ldapImportData) {
        applyKlassen(user, realm, ldapImportData.klassen());
        applyRolle(user, realm, ldapImportData.rolle() != null ? ldapImportData.rolle().toString() : null);
    }

    private void applyKlassen(final UserModel user, final RealmModel realm, final List<KlasseData> klassen) {
        for (final var klasse : klassen) {
            applyKlasse(user, realm, klasse);
        }
    }

    private void applyKlasse(final UserModel user, final RealmModel realm, final KlasseData klasse) {
        var groupModel = groupProvider.getGroupsStream(realm)
                .filter(gm -> gm.getName().equals(klasse.name()))
                .findFirst()
                .orElseGet(() -> groupProvider.createGroup(realm, klasse.name()));

        user.joinGroup(groupModel);

        LOGGER.infof("Assigned user %s to group '%s'", user.getId(), groupModel.getName());
    }

    private void applyRolle(final UserModel user, final RealmModel realm, final String rolle) {
        if (rolle == null) {
            LOGGER.warn("Can't apply role. Value is null");
            return;
        }

        var roleModel = roleProvider.getRealmRole(realm, rolle);
        if (roleModel == null) {
            roleModel = roleProvider.addRealmRole(realm, rolle);
        }

        user.grantRole(roleModel);

        LOGGER.infof("Granted role '%s' to user %s", rolle, user.getId());
    }

    private PersonData extractPersonData(final UserModel user, final LDAPObject ldapUser) {
        final var keycloakUserId = user.getId();
        final var firstName = ldapUser.getAttributeAsString(config.getFirst(CONFIG_KEY_PERSON_FIRSTNAME_ATTR));
        final var lastName = ldapUser.getAttributeAsString(config.getFirst(CONFIG_KEY_PERSON_LASTNAME_ATTR));
        final var email = ldapUser.getAttributeAsString(config.getFirst(CONFIG_KEY_PERSON_EMAIL_ATTR));
        final var birthday = getBirthday(ldapUser);
        final var ldapDn = ldapUser.getDn().toString();

        return new PersonData(keycloakUserId, firstName, lastName, email, birthday, ldapDn);
    }

    private SchuleData extractSchuleData() {
        final var schoolName = config.getFirst(CONFIG_KEY_SCHULE_NAME);
        final var zugehoerigZu = config.getFirst(CONFIG_KEY_SCHULE_ZUGEHOERIG_ZU);
        final var externalId = config.getFirst(CONFIG_KEY_SCHULE_EXTERNAL_ID);

        return new SchuleData(schoolName, zugehoerigZu, externalId);
    }

    private List<KlasseData> extractKlassenData(final String userDn) {
        try (final var query = new LDAPQuery(ldapStorageProvider)) {
            final var searchBase = config.getFirst(CONFIG_KEY_KLASSE_OU);
            final var memberAttr = config.getFirst(CONFIG_KEY_KLASSE_MEMBER_FIELD);
            query.setSearchDn(searchBase);
            query.setSearchScope(SearchControls.SUBTREE_SCOPE);
            query.addWhereCondition(LDAP_QUERY_CONDITIONS_BUILDER.equal(memberAttr, userDn));

            final var klassenNameAttr = config.getFirst(CONFIG_KEY_KLASSE_NAME_ATTR);
            return query.getResultList().stream().map(ldapObject -> {
                final var klassenName = readAttributeOrRdn(ldapObject, klassenNameAttr).orElse(null);
                final var externalClassId = ldapObject.getDn().toString();
                return new KlasseData(klassenName, externalClassId);
            }).toList();
        }
    }

    private ErwinRollenArt extractRolle(final LDAPObject user) {
        final var mappingType = config.getFirst(CONFIG_KEY_ROLLE_MAPPING_TYPE);
        return switch (mappingType) {
            case "Field" -> extractRolleFromField(user);
            case "Group memberOf" -> extractRolleFromGroupMemberOf(user);
            case "Group member" -> extractRolleFromGroupMember(user);
            default ->
                    throw new IllegalStateException("Unexpected value for rolle mapping type: " + config.getFirst(CONFIG_KEY_ROLLE_MAPPING_TYPE));
        };
    }

    private ErwinRollenArt extractRolleFromField(LDAPObject user) {
        final var userRole = user.getAttributeAsString(config.getFirst(CONFIG_KEY_ROLLE_FIELD));

        return mapRolle(userRole, user.getDn().toString());
    }

    private ErwinRollenArt extractRolleFromGroupMemberOf(LDAPObject user) {
        final var roleLehr = config.getList(CONFIG_KEY_ROLLE_LEHR);
        final var roleLern = config.getList(CONFIG_KEY_ROLLE_LERN);
        final var roleLeit = config.getList(CONFIG_KEY_ROLLE_LEIT);

        final var userRole = user.getAttributeAsString(config.getFirst(CONFIG_KEY_ROLLE_FIELD));

        if (roleLehr.contains(userRole)) {
            return ErwinRollenArt.LEHR;
        } else if (roleLern.contains(userRole)) {
            return ErwinRollenArt.LERN;
        } else if (roleLeit.contains(userRole)) {
            return ErwinRollenArt.LEIT;
        } else {
            return null;
        }
    }

    private ErwinRollenArt extractRolleFromGroupMember(LDAPObject user) {
        try (final var query = new LDAPQuery(ldapStorageProvider)) {
            final var searchBase = config.getFirst(CONFIG_KEY_ROLLE_OU);
            final var memberAttr = config.getFirst(CONFIG_KEY_ROLLE_FIELD);
            query.setSearchDn(searchBase);
            query.setSearchScope(SearchControls.SUBTREE_SCOPE);
            query.addWhereCondition(LDAP_QUERY_CONDITIONS_BUILDER.equal(memberAttr, user.getDn()));

            final var result = query.getFirstResult();
            final var roleNameAttr = config.getFirst(CONFIG_KEY_ROLLE_NAME_ATTR);
            final var role = readAttributeOrRdn(result, roleNameAttr).orElse(null);

            return mapRolle(role, user.getDn().toString());
        }
    }

    private ErwinRollenArt mapRolle(final String role, String userDn) {
        if (role == null) {
            LOGGER.warnf("Can't map null role value. UserDn: %s", userDn);
            return null;
        }

        final var roleLehr = config.getFirst(CONFIG_KEY_ROLLE_LEHR);
        final var roleLern = config.getFirst(CONFIG_KEY_ROLLE_LERN);
        final var roleLeit = config.getFirst(CONFIG_KEY_ROLLE_LEIT);

        if (roleLehr.equals(role)) {
            return ErwinRollenArt.LEHR;
        } else if (roleLern.equals(role)) {
            return ErwinRollenArt.LERN;
        } else if (roleLeit.equals(role)) {
            return ErwinRollenArt.LEIT;
        } else {
            return null;
        }
    }

    private LocalDate getBirthday(final LDAPObject ldapUser) {
        final var birthdayValue = ldapUser.getAttributeAsString(config.getFirst(CONFIG_KEY_PERSON_BIRTHDAY_ATTR));

        if (birthdayValue == null) {
            LOGGER.warnf("No birthday found for user dn '%s'", ldapUser.getDn());
            return null;
        }

        final var format = switch (config.getFirst(CONFIG_KEY_PERSON_BIRTHDAY_ATTR_TYPE)) {
            case "String" -> FORMATTER_STRING;
            case "GeneralizedTime" -> FORMATTER_GENERALIZED_TIME;
            default ->
                    throw new IllegalStateException("Unexpected value for birthday attribute type: " + config.getFirst(CONFIG_KEY_PERSON_BIRTHDAY_ATTR_TYPE));
        };

        return LocalDate.parse(birthdayValue, format);
    }

    private static Optional<String> readAttributeOrRdn(final LDAPObject ldapObject, final String name) {
        return Optional.ofNullable(ldapObject.getAttributeAsString(name))
                .or(() -> ldapObject
                        // Get RDNs
                        .getDn().getLdapName().getRdns()
                        // Filter for RDN with given name
                        .stream().filter(rdn -> rdn.getType().equalsIgnoreCase(name)).map(Rdn::getValue).map(Object::toString)
                        .findFirst());
    }

    // empty implementations from abstract class. Not required for this plugin
    @Override
    public void onRegisterUserToLDAP(LDAPObject ldapUser, UserModel localUser, RealmModel realm) {

    }

    @Override
    public void beforeLDAPQuery(LDAPQuery query) {

    }

    @Override
    public UserModel proxy(LDAPObject ldapUser, UserModel delegate, RealmModel realm) {
        return delegate;
    }

}