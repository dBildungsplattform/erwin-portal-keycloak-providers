package com.spsh.ldap.dto;

import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

public class PersonDataTest {

    @Test
    public void toJson() {
        final var keycloakUserId = "keycloakUserId";
        final var vorname = "Bob";
        final var nachname = "Smith";
        final var email = "bob.smith@example.org";
        final var geburtstag = LocalDate.now();
        final var externalId = "extId";

        final var personData = new PersonData(keycloakUserId, vorname, nachname, email, geburtstag, externalId);

        final var json = personData.toJson();

        assertNotNull(json);
        assertEquals(keycloakUserId, json.get("keycloakUserId"));
        assertEquals(vorname, json.get("vorname"));
        assertEquals(nachname, json.get("nachname"));
        assertEquals(email, json.get("email"));
        assertEquals(geburtstag.toString(), json.get("geburtstag"));
        assertEquals(externalId, json.get("externalId"));
    }
}