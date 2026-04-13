package com.spsh.ldap;

import org.keycloak.provider.ProviderConfigProperty;
import org.keycloak.provider.ProviderConfigurationBuilder;

import java.util.List;

public class ErwinLdapMapperConfig {

    public static List<ProviderConfigProperty> CONFIG_PROPERTIES;

    public static final String ERWIN_LDAP_MAPPER_PREFIX = "erwin_ldap_mapper.";

    public static final String CONFIG_KEY_SERVER_NEW_LDAP_USER_URL = ERWIN_LDAP_MAPPER_PREFIX + "server.new_ldap_user_url";

    // Person
    public static final String PERSON_VALUES_PREFIX = ERWIN_LDAP_MAPPER_PREFIX + "person.";

    public static final String CONFIG_KEY_PERSON_FIRSTNAME_ATTR = PERSON_VALUES_PREFIX + "firstname_attr";
    public static final String CONFIG_KEY_PERSON_LASTNAME_ATTR = PERSON_VALUES_PREFIX + "lastname_attr";
    public static final String CONFIG_KEY_PERSON_EMAIL_ATTR = PERSON_VALUES_PREFIX + "email_attr";
    public static final String CONFIG_KEY_PERSON_BIRTHDAY_ATTR = PERSON_VALUES_PREFIX + "birthday_attr";
    public static final String CONFIG_KEY_PERSON_BIRTHDAY_ATTR_TYPE = PERSON_VALUES_PREFIX + "birthday_attr_type";
    public static final String[] CONFIG_KEY_PERSON_BIRTHDAY_ATTR_TYPE_OPTIONS = {"String", "GeneralizedTime"};

    // Schule
    public static final String SCHULE_VALUES_PREFIX = ERWIN_LDAP_MAPPER_PREFIX + "schule.";

    public static final String CONFIG_KEY_SCHULE_NAME = SCHULE_VALUES_PREFIX + "name";
    public static final String CONFIG_KEY_SCHULE_ZUGEHOERIG_ZU = SCHULE_VALUES_PREFIX + "zugehoerig_zu";
    public static final String CONFIG_KEY_SCHULE_EXTERNAL_ID = SCHULE_VALUES_PREFIX + "external_id";

    // Klasse
    public static final String KLASSE_VALUES_PREFIX = ERWIN_LDAP_MAPPER_PREFIX + "schule.";

    public static final String CONFIG_KEY_KLASSE_OU = KLASSE_VALUES_PREFIX + "ou";
    public static final String CONFIG_KEY_KLASSE_NAME_ATTR = KLASSE_VALUES_PREFIX + "name_attr";
    public static final String CONFIG_KEY_KLASSE_MEMBER_FIELD = KLASSE_VALUES_PREFIX + "member_field";

    // Rolle
    public static final String ROLLE_VALUES_PREFIX = ERWIN_LDAP_MAPPER_PREFIX + "rolle.";

    public static final String CONFIG_KEY_ROLLE_MAPPING_TYPE = ROLLE_VALUES_PREFIX + "mapping_type";
    public static final String[] CONFIG_KEY_ROLLE_MAPPING_TYPES = {"Field", "Group memberOf", "Group member"};
    public static final String CONFIG_KEY_ROLLE_OU = ROLLE_VALUES_PREFIX + "ou";
    public static final String CONFIG_KEY_ROLLE_FIELD = ROLLE_VALUES_PREFIX + "field";
    public static final String CONFIG_KEY_ROLLE_NAME_ATTR = ROLLE_VALUES_PREFIX + "name_attr";
    public static final String CONFIG_KEY_ROLLE_LEHR = ROLLE_VALUES_PREFIX + "lehr";
    public static final String CONFIG_KEY_ROLLE_LERN = ROLLE_VALUES_PREFIX + "lern";
    public static final String CONFIG_KEY_ROLLE_LEIT = ROLLE_VALUES_PREFIX + "leit";

    static {
        CONFIG_PROPERTIES = ProviderConfigurationBuilder.create()
                // Url to send a new user
                .property()
                .name(CONFIG_KEY_SERVER_NEW_LDAP_USER_URL)
                .label("New LDAP user url")
                .type(ProviderConfigProperty.STRING_TYPE)
                .required(true)
                .add()

                // LDAP attribute for the email
                .property()
                .name(CONFIG_KEY_PERSON_EMAIL_ATTR)
                .label("Email LDAP Attribute")
                .type(ProviderConfigProperty.STRING_TYPE)
                .defaultValue("mail")
                .required(true)
                .add()

                // LDAP attribute for the first name
                .property()
                .name(CONFIG_KEY_PERSON_FIRSTNAME_ATTR)
                .label("First name LDAP Attribute")
                .type(ProviderConfigProperty.STRING_TYPE)
                .defaultValue("givenName")
                .required(true)
                .add()

                // LDAP attribute for the last name
                .property()
                .name(CONFIG_KEY_PERSON_LASTNAME_ATTR)
                .label("Last name LDAP Attribute")
                .type(ProviderConfigProperty.STRING_TYPE)
                .defaultValue("sn")
                .required(true)
                .add()

                // LDAP attributes for the birthday
                .property()
                .name(CONFIG_KEY_PERSON_BIRTHDAY_ATTR)
                .label("Birthday LDAP Attribute")
                .type(ProviderConfigProperty.STRING_TYPE)
                .defaultValue("dateOfBirth")
                .required(true)
                .add()

                .property()
                .name(CONFIG_KEY_PERSON_BIRTHDAY_ATTR_TYPE)
                .label("Birthday LDAP Attribute Type")
                .type(ProviderConfigProperty.LIST_TYPE)
                .options(CONFIG_KEY_PERSON_BIRTHDAY_ATTR_TYPE_OPTIONS)
                .defaultValue(CONFIG_KEY_PERSON_BIRTHDAY_ATTR_TYPE_OPTIONS[0])
                .required(true)
                .add()

                // The name of the school
                .property()
                .name(CONFIG_KEY_SCHULE_NAME)
                .label("School name")
                .type(ProviderConfigProperty.STRING_TYPE)
                .required(true)
                .add()

                // The zugehoerigZu relation of the school
                .property()
                .name(CONFIG_KEY_SCHULE_ZUGEHOERIG_ZU)
                .label("School zugehoerigZu")
                .type(ProviderConfigProperty.STRING_TYPE)
                .required(true)
                .add()

                // The externalId relation of the school
                .property()
                .name(CONFIG_KEY_SCHULE_EXTERNAL_ID)
                .label("School externalId")
                .type(ProviderConfigProperty.STRING_TYPE)
                .required(true)
                .add()

                // Base DN/OU for klassen
                .property()
                .name(CONFIG_KEY_KLASSE_OU)
                .label("OU of classes")
                .type(ProviderConfigProperty.STRING_TYPE)
                .required(true)
                .add()

                // Field for the member assignment of persons
                .property()
                .name(CONFIG_KEY_KLASSE_MEMBER_FIELD)
                .label("Member field of a class")
                .type(ProviderConfigProperty.STRING_TYPE)
                .defaultValue("member")
                .required(true)
                .add()

                // LDAP attribute for the name of a class
                .property()
                .name(CONFIG_KEY_KLASSE_NAME_ATTR)
                .label("Name of a class")
                .type(ProviderConfigProperty.STRING_TYPE)
                .defaultValue("cn")
                .required(true)
                .add()

                // Mapping type for roles
                .property()
                .name(CONFIG_KEY_ROLLE_MAPPING_TYPE)
                .label("Mapping type of the role")
                .type(ProviderConfigProperty.LIST_TYPE)
                .options(CONFIG_KEY_ROLLE_MAPPING_TYPES)
                .defaultValue(CONFIG_KEY_ROLLE_MAPPING_TYPES[0])
                .required(true)
                .add()

                // Base DN/OU for rollen (for mapping type 'Group member')
                .property()
                .name(CONFIG_KEY_ROLLE_OU)
                .label("OU of roles")
                .type(ProviderConfigProperty.STRING_TYPE)
                .required(false)
                .add()

                // Field for the member assignment of persons
                .property()
                .name(CONFIG_KEY_ROLLE_FIELD)
                .label("Field which defines the role")
                .type(ProviderConfigProperty.STRING_TYPE)
                .defaultValue("role")
                .required(true)
                .add()

                // Name of a role of persons
                .property()
                .name(CONFIG_KEY_ROLLE_NAME_ATTR)
                .label("Name of the role")
                .type(ProviderConfigProperty.STRING_TYPE)
                .defaultValue("cn")
                .required(false)
                .add()

                // Value which identifies a LEHR rolle
                .property()
                .name(CONFIG_KEY_ROLLE_LEHR)
                .label("Value which identifies a LEHR role")
                .type(ProviderConfigProperty.STRING_TYPE)
                .defaultValue("ROLLE_LEHR")
                .required(true)
                .add()

                // Value which identifies a LERN rolle
                .property()
                .name(CONFIG_KEY_ROLLE_LERN)
                .label("Value which identifies a LERN role")
                .type(ProviderConfigProperty.STRING_TYPE)
                .defaultValue("ROLLE_LERN")
                .required(true)
                .add()

                // Value which identifies a LEIT rolle
                .property()
                .name(CONFIG_KEY_ROLLE_LEIT)
                .label("Value which identifies a LEIT role")
                .type(ProviderConfigProperty.STRING_TYPE)
                .defaultValue("ROLLE_LEIT")
                .required(true)
                .add()

                .build();
    }

}
