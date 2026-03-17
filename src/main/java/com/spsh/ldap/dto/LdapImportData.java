package com.spsh.ldap.dto;

import org.json.JSONObject;

/**
 * The data imported into the ErWInPortal for each LDAP user.
 *
 * @param person the person data
 * @param schule the school data
 * @param klasse the class data
 * @param rolle  the role
 */
public record LdapImportData(PersonData person, SchuleData schule, KlasseData klasse, ErwinRollenArt rolle) {

    public JSONObject toJson() {
        JSONObject json = new JSONObject();
        json.put("person", person.toJson());
        json.put("schule", schule.toJson());
        json.put("klasse", klasse.toJson());
        json.put("rolle", rolle.toString());

        return json;
    }

}
