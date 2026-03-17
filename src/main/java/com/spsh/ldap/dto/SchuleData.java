package com.spsh.ldap.dto;

import org.json.JSONObject;

/**
 * The school data imported into the ErWInPortal for a user.
 *
 * @param name         the name of the school
 * @param zugehoerigZu the organization this schools belongs to
 * @param externalId   the externalId i.e. the LDAP ou
 */
public record SchuleData(String name, String zugehoerigZu, String externalId) {

    public JSONObject toJson() {
        JSONObject json = new JSONObject();
        json.put("name", name);
        json.put("zugehoerigZu", zugehoerigZu);
        json.put("externalId", externalId);

        return json;
    }

}
