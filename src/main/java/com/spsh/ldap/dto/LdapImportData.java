package com.spsh.ldap.dto;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.List;

/**
 * The data imported into the ErWInPortal for each LDAP user.
 *
 * @param person  the person data
 * @param schule  the school data
 * @param klassen the class data
 * @param rolle   the role
 */
public record LdapImportData(PersonData person, SchuleData schule, List<KlasseData> klassen, ErwinRollenArt rolle) {

    public JSONObject toJson() {
        JSONObject json = new JSONObject();
        json.put("person", person.toJson());
        json.put("schule", schule.toJson());
        json.put("rolle", rolle.toString());

        final List<JSONObject> klassenObjects = klassen.stream().map(KlasseData::toJson).toList();
        json.put("klassen", new JSONArray(klassenObjects));

        return json;
    }

}
