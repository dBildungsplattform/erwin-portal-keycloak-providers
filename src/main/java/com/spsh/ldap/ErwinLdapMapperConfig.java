package com.spsh.ldap;

import org.keycloak.provider.ProviderConfigProperty;
import org.keycloak.provider.ProviderConfigurationBuilder;

import java.util.List;

public class ErwinLdapMapperConfig {

    public static List<ProviderConfigProperty> CONFIG_PROPERTIES;

    public static final String CONFIG_KEY_SERVER_NEW_LDAP_USER_URL = "erwin_ldap_mapper.server.new_ldap_user_url";

    // Person
    public static final String CONFIG_KEY_PERSON_FIRSTNAME_ATTR = "erwin_ldap_mapper.person.firstname_attr";
    public static final String CONFIG_KEY_PERSON_LASTNAME_ATTR = "erwin_ldap_mapper.person.lastname_attr";
    public static final String CONFIG_KEY_PERSON_EMAIL_ATTR = "erwin_ldap_mapper.person.email_attr";
    public static final String CONFIG_KEY_PERSON_BIRTHDAY_ATTR = "erwin_ldap_mapper.person.birthday_attr";
    public static final String CONFIG_KEY_PERSON_BIRTHDAY_ATTR_TYPE = "erwin_ldap_mapper.person.birthday_attr_type";
    public static final String[] CONFIG_KEY_PERSON_BIRTHDAY_ATTR_TYPE_OPTIONS = {"String", "GeneralizedTime"};

    // Schule
    public static final String CONFIG_KEY_SCHULE_NAME = "erwin_ldap_mapper.schule.name";
    public static final String CONFIG_KEY_SCHULE_ZUGEHOERIG_ZU = "erwin_ldap_mapper.schule.zugehoerig_zu";
    public static final String CONFIG_KEY_SCHULE_EXTERNAL_ID = "erwin_ldap_mapper.schule.external_id";

    // Klasse
    public static final String CONFIG_KEY_KLASSE_OU = "erwin_ldap_mapper.klasse.ou";
    public static final String CONFIG_KEY_KLASSE_NAME_ATTR = "erwin_ldap_mapper.klasse.name_attr";
    public static final String CONFIG_KEY_KLASSE_MEMBER_FIELD = "erwin_ldap_mapper.klasse.member_field";

    // Rolle
    public static final String CONFIG_KEY_ROLLE_MAPPING_TYPE = "erwin_ldap_mapper.rolle.mapping_type";
    public static final String[] CONFIG_KEY_ROLLE_MAPPING_TYPES = {"Field", "Group memberOf", "Group member"};
    public static final String CONFIG_KEY_ROLLE_OU = "erwin_ldap_mapper.rolle.ou";
    public static final String CONFIG_KEY_ROLLE_FIELD = "erwin_ldap_mapper.rolle.field";
    public static final String CONFIG_KEY_ROLLE_NAME_ATTR = "erwin_ldap_mapper.rolle.name_attr";
    public static final String CONFIG_KEY_ROLLE_LEHR = "erwin_ldap_mapper.rolle.lehr";
    public static final String CONFIG_KEY_ROLLE_LERN = "erwin_ldap_mapper.rolle.lern";
    public static final String CONFIG_KEY_ROLLE_LEIT = "erwin_ldap_mapper.rolle.leit";

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

                // Base DN/OU for rollen (for mapping tyep 'Group member')
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
