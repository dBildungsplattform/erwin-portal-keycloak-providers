package com.spsh.util;

import com.jayway.jsonpath.JsonPath;
import com.jayway.jsonpath.PathNotFoundException;
import com.spsh.oidc.dto.FetchUrlResponse;

public class JsonHelper {

    /**
     * True if the JSONPath exists (value not null).
     */
    public static boolean isPathExisting(String jsonData, String jsonPath) {
        try {
            JsonPath.read(jsonData, jsonPath);
            return true;
        } catch (PathNotFoundException e) {
            return false;
        }
    }

    /**
     * Extract value at JSONPath
     */
    public static FetchUrlResponse extractFromJson(String jsonData) {
        FetchUrlResponse extractedResponse = new FetchUrlResponse();
        extractedResponse.setUserId(JsonPath.read(jsonData, "$.userId"));
        extractedResponse.setMapToLmsRolle(JsonPath.read(jsonData, "$.mapToLmsRolle"));

        if (!extractedResponse.getUserId().isEmpty() || !extractedResponse.getMapToLmsRolle().isEmpty()) {
            return extractedResponse;
        } else {
            return null;
        }
    }

    /**
     * Helper for JSON Escaping
     */
    public static String jsonString(String s) {
        if (s == null) return "null";
        return "\"" + s.replace("\\", "\\\\").replace("\"", "\\\"") + "\"";
    }
}
