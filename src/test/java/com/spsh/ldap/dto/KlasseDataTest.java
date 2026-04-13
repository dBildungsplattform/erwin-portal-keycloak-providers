package com.spsh.ldap.dto;

import org.junit.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class KlasseDataTest {

    @Test
    public void toJson() {
        final var name = "9b";
        final var externalId = "extId";

        final var klasseData = new KlasseData(name, externalId);

        final var json = klasseData.toJson();

        assertNotNull(json);
        assertEquals(name, json.get("name"));
        assertEquals(externalId, json.get("externalId"));
    }
}
