package com.spsh.ldap.dto;

import org.json.JSONObject;

/**
 * The class data imported into the ErWInPortal for a user.
 *
 * @param name         the name of the class
 * @param externalId   the externalId i.e. the LDAP dn
 */
public record KlasseData(String name, String externalId) {

    public JSONObject toJson() {
        JSONObject json = new JSONObject();
        json.put("name", name);
        json.put("externalId", externalId);

        return json;
    }

}
