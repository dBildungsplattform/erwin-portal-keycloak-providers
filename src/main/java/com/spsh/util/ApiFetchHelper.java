package com.spsh.util;

import java.io.IOException;

import org.apache.hc.client5.http.classic.methods.HttpPost;
// (GET import kept only if you need it later)
// import org.apache.hc.client5.http.classic.methods.HttpGet;
import org.apache.hc.client5.http.config.RequestConfig;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.HttpClients;

import org.apache.hc.core5.http.HttpEntity;
import org.apache.hc.core5.http.io.entity.EntityUtils;
import org.apache.hc.core5.http.io.entity.StringEntity;

import org.apache.hc.core5.util.Timeout;

import com.jayway.jsonpath.JsonPath;
import com.jayway.jsonpath.PathNotFoundException;

public class ApiFetchHelper {

    public static final String ENV_KEY_INTERNAL_COMMUNICATION_API_KEY = "INTERNAL_COMMUNICATION_API_KEY";

    public static String fetchApiData(String url, String userSub) throws IOException {

        String apiKey = System.getenv(ENV_KEY_INTERNAL_COMMUNICATION_API_KEY);
        if (apiKey == null || apiKey.isEmpty()) {
            throw new IOException(String.format("Environment variable %s is not set or is empty.", ENV_KEY_INTERNAL_COMMUNICATION_API_KEY));
        }

        try (CloseableHttpClient httpClient = HttpClients.createDefault()) {
            HttpPost request = new HttpPost(url);
            request.setHeader("Content-Type", "application/json");
            request.setHeader("api-key", apiKey);
            StringEntity requestBody = new StringEntity(String.format("{\"sub\":\"%s\"}", userSub));
            request.setEntity(requestBody);

            return httpClient.execute(request, response -> {
                int statusCode = response.getCode();
                if (statusCode >= 200 && statusCode < 300) {
                    HttpEntity entity = response.getEntity();
                    return entity != null ? EntityUtils.toString(entity) : null;
                } else {
                    throw new IOException("Unexpected response status: " + statusCode);
                }
            });
        }
    }

    public static String fetchApiData(String url,
                                      String userId,
                                      String clientId,
                                      String clientName,
                                      String headerName,
                                      int timeoutMs) throws IOException {

        String apiKey = System.getenv(ENV_KEY_INTERNAL_COMMUNICATION_API_KEY);
        if (apiKey == null || apiKey.isEmpty()) {
            throw new IOException(String.format("Environment variable %s is not set or is empty.", ENV_KEY_INTERNAL_COMMUNICATION_API_KEY));
        }

        String header = (headerName == null || headerName.isBlank()) ? "api-key" : headerName;

        RequestConfig requestConfig = RequestConfig.custom()
                .setConnectTimeout(Timeout.ofMilliseconds(timeoutMs))
                .setResponseTimeout(Timeout.ofMilliseconds(timeoutMs))
                .build();

        try (CloseableHttpClient httpClient =
                     HttpClients.custom().setDefaultRequestConfig(requestConfig).build()) {

            HttpPost request = new HttpPost(url);
            request.setHeader("Content-Type", "application/json");
            request.setHeader(header, apiKey);

            String payload = "{"
                    + "\"userId\":" + jsonString(userId) + ","
                    + "\"clientId\":" + jsonString(clientId) + ","
                    + "\"clientName\":" + jsonString(clientName)
                    + "}";

            request.setEntity(new StringEntity(payload));

            return httpClient.execute(request, response -> {
                int statusCode = response.getCode();
                if (statusCode >= 200 && statusCode < 300) {
                    HttpEntity entity = response.getEntity();
                    return entity != null ? EntityUtils.toString(entity) : null;
                } else {
                    throw new IOException("Unexpected response status: " + statusCode);
                }
            });
        }
    }

    /** True if the JSONPath exists (value not null). */
    public static boolean isPathExisting(String jsonData, String jsonPath) {
        try {
            JsonPath.read(jsonData, jsonPath);
            return true;
        } catch (PathNotFoundException e) {
            return false;
        }
    }

    /** Extract value at JSONPath. Arrays become java.util.List; objects are Map; scalars are String/Number/Boolean. */
    public static Object extractFromJson(String jsonData, String jsonPath) {
        return JsonPath.read(jsonData, jsonPath);
    }

    // --- small local helper for JSON escaping ---
    private static String jsonString(String s) {
        if (s == null) return "null";
        return "\"" + s.replace("\\","\\\\").replace("\"","\\\"") + "\"";
    }
}