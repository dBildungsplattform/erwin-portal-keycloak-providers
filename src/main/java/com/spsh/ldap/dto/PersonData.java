package com.spsh.ldap.dto;

import org.json.JSONObject;

import java.time.LocalDate;

/**
 * The person data imported into the ErWInPortal for a user.
 *
 * @param keycloakUserId the keycloakUserId
 * @param vorname        the first name of the user
 * @param nachname       the last name of the user
 * @param email          the email address of the user
 * @param geburtstag     the birthday of the user
 * @param externalId     the externalId i.e. the LDAP dn
 */
public record PersonData(String keycloakUserId, String vorname, String nachname, String email, LocalDate geburtstag,
                         String externalId) {

    public JSONObject toJson() {
        JSONObject json = new JSONObject();
        json.put("keycloakUserId", keycloakUserId);
        json.put("vorname", vorname);
        json.put("nachname", nachname);
        json.put("email", email);
        json.put("geburtstag", geburtstag.toString());
        json.put("externalId", externalId);

        return json;
    }

}
