package com.spsh.util;

import com.jayway.jsonpath.PathNotFoundException;
import com.spsh.oidc.dto.FetchUrlResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class JsonHelperTest {

    private String validJson;
    private String missingFieldsJson;
    private String emptyValuesJson;

    @BeforeEach
    void setUp() {
        validJson = """
        {
          "userId": "user-123",
          "mapToLmsRolle": "Admin"
        }
        """;

        missingFieldsJson = """
        {
          "userId": "user-123"
        }
        """;

        emptyValuesJson = """
        {
          "userId": "",
          "mapToLmsRolle": ""
        }
        """;
    }

    // ---------------------------
    // isPathExisting(...)
    // ---------------------------

    @Test
    void isPathExisting_returnsTrue_whenPathIsPresent() {
        boolean existsUserId = JsonHelper.isPathExisting(validJson, "$.userId");
        boolean existsRole = JsonHelper.isPathExisting(validJson, "$.mapToLmsRolle");

        assertTrue(existsUserId);
        assertTrue(existsRole);
    }

    @Test
    void isPathExisting_returnsFalse_whenPathIsMissing() {
        boolean exists = JsonHelper.isPathExisting(missingFieldsJson, "$.mapToLmsRolle");
        assertFalse(exists);
    }

    // ---------------------------
    // extractFromJson(...)
    // ---------------------------

    @Test
    void extractFromJson_returnsObject_whenAnyValueNonEmpty() {
        FetchUrlResponse res = JsonHelper.extractFromJson(validJson);

        assertNotNull(res, "Expected non-null FetchUrlResponse when values exist");
        assertEquals("user-123", res.getUserId());
        assertEquals("Admin", res.getMapToLmsRolle());
    }

    @Test
    void extractFromJson_returnsNull_whenBothValuesAreEmptyStrings() {
        FetchUrlResponse res = JsonHelper.extractFromJson(emptyValuesJson);
        assertNull(res, "Expected null when both userId and mapToLmsRolle are empty");
    }

    @Test
    void extractFromJson_throwsPathNotFound_whenFieldMissing() {
        // Because extractFromJson() calls JsonPath.read(...) directly (no try/catch),
        // a missing field leads to a PathNotFoundException.
        assertThrows(PathNotFoundException.class, () -> JsonHelper.extractFromJson(missingFieldsJson));
    }

    // ---------------------------
    // jsonString(...)
    // ---------------------------

    @Test
    void jsonString_returnsQuotedAndEscapedValue() {
        String input = "He said: \"Hi\" \\ ok";
        String escaped = JsonHelper.jsonString(input);
        assertEquals("\"He said: \\\"Hi\\\" \\\\ ok\"", escaped);
    }

    @Test
    void jsonString_returnsLiteralNull_whenInputIsNull() {
        assertEquals("null", JsonHelper.jsonString(null));
    }
}