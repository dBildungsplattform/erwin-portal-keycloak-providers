package com.spsh.ldap.dto;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.Collections;
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
        json.put("person", person != null ? person.toJson() : null);
        json.put("schule", schule != null ? schule.toJson() : null);
        json.put("rolle", rolle != null ? rolle.toString() : null);

        final List<JSONObject> klassenObjects = klassen != null ? klassen.stream().map(KlasseData::toJson).toList() : Collections.emptyList();
        json.put("klassen", new JSONArray(klassenObjects));

        return json;
    }

}
