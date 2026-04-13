package com.spsh.ldap.dto;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class SchuleDataTest {

    @Test
    public void toJson() {
        final var name = "Schule";
        final var zugehoerigZu = "zugehoerigZu";
        final var externalId = "extId";

        final var schuleData = new SchuleData(name, zugehoerigZu, externalId);

        final var json = schuleData.toJson();

        assertNotNull(json);
        assertEquals(name, json.get("name"));
        assertEquals(zugehoerigZu, json.get("zugehoerigZu"));
        assertEquals(externalId, json.get("externalId"));
    }
}