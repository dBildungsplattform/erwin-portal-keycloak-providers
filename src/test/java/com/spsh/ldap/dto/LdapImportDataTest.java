package com.spsh.ldap.dto;

import org.json.JSONArray;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class LdapImportDataTest {

    @Test
    public void toJson() {
        final var keycloakUserId = "keycloakUserId";
        final var vorname = "Bob";
        final var nachname = "Smith";
        final var email = "bob.smith@example.org";
        final var geburtstag = LocalDate.now();
        final var personExternalId = "personExtId";

        final var personData = new PersonData(keycloakUserId, vorname, nachname, email, geburtstag, personExternalId);

        final var klasseName = "9b";
        final var klasseExternalId = "klasseExtId";

        final var klasseData = new KlasseData(klasseName, klasseExternalId);

        final var schuleName = "Schule";
        final var zugehoerigZu = "zugehoerigZu";
        final var schuleExternalId = "schuleExtId";

        final var schuleData = new SchuleData(schuleName, zugehoerigZu, schuleExternalId);

        final var ldapImportData = new LdapImportData(personData, schuleData, List.of(klasseData), ErwinRollenArt.LEHR);

        final var jsonImportData = ldapImportData.toJson();

        assertNotNull(jsonImportData);

        final var personJson = jsonImportData.get("person");
        assertEquals(personData.toJson().toString(), personJson.toString());

        final var schuleJson = jsonImportData.get("schule");
        assertEquals(schuleData.toJson().toString(), schuleJson.toString());

        final var klassenJson = jsonImportData.get("klassen");
        assertEquals(new JSONArray(List.of(klasseData.toJson())).toString(), klassenJson.toString());

        final var rolle = jsonImportData.get("rolle");
        assertEquals(ErwinRollenArt.LEHR.toString(), rolle);
    }
}