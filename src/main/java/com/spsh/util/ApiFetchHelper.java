package com.spsh.util;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpRequest.BodyPublishers;
import java.net.http.HttpResponse.BodyHandlers;

import org.apache.hc.client5.http.classic.methods.HttpPost;
import org.apache.hc.client5.http.config.RequestConfig;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.HttpClients;

import org.apache.hc.core5.http.HttpEntity;
import org.apache.hc.core5.http.io.entity.EntityUtils;
import org.apache.hc.core5.http.io.entity.StringEntity;

import org.apache.hc.core5.util.Timeout;
import org.json.JSONObject;

public class ApiFetchHelper {

    public static final String ENV_KEY_INTERNAL_COMMUNICATION_API_KEY = "INTERNAL_COMMUNICATION_API_KEY";

    public static String fetchApiData(String url, String userSub) throws IOException {

        String apiKey = System.getenv(ENV_KEY_INTERNAL_COMMUNICATION_API_KEY);
        if (apiKey == null || apiKey.isEmpty()) {
            throw new IOException(String.format("Environment variable %s is not set or is empty.",
                    ENV_KEY_INTERNAL_COMMUNICATION_API_KEY));
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
            String clientName,
            String headerName,
            int timeoutMs) throws IOException {

        String apiKey = System.getenv(ENV_KEY_INTERNAL_COMMUNICATION_API_KEY);
        if (apiKey == null || apiKey.isEmpty()) {
            throw new IOException(String.format("Environment variable %s is not set or is empty.",
                    ENV_KEY_INTERNAL_COMMUNICATION_API_KEY));
        }

        String header = (headerName == null || headerName.isBlank()) ? "api-key" : headerName;

        RequestConfig requestConfig = RequestConfig.custom()
                .setConnectTimeout(Timeout.ofMilliseconds(timeoutMs))
                .setResponseTimeout(Timeout.ofMilliseconds(timeoutMs))
                .build();

        try (CloseableHttpClient httpClient = HttpClients.custom().setDefaultRequestConfig(requestConfig).build()) {

            HttpPost request = new HttpPost(url);
            request.setHeader("Content-Type", "application/json");
            request.setHeader(header, apiKey);

            String payload = "{"
                    + "\"userId\":" + JsonHelper.jsonString(userId) + ","
                    + "\"clientName\":" + JsonHelper.jsonString(clientName)
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

    public static void sendLdapUserData(URI uri, String keycloakUserId, String userName, String email,
            String firstName,
            String lastName, String ldapDn, String ldapId) throws IOException, InterruptedException {
        final var apiKey = System.getenv(ENV_KEY_INTERNAL_COMMUNICATION_API_KEY);
        if (apiKey == null || apiKey.isEmpty()) {
            throw new IOException(String.format("Environment variable %s is not set or is empty.",
                    ENV_KEY_INTERNAL_COMMUNICATION_API_KEY));
        }

        final var json = new JSONObject();
        json.put("keycloakUserId", keycloakUserId);
        json.put("userName", userName);
        json.put("email", email);
        json.put("firstName", firstName);
        json.put("lastName", lastName);
        json.put("ldapDn", ldapDn);
        json.put("ldapId", ldapId);

        final var request = HttpRequest.newBuilder(uri)
                .POST(BodyPublishers.ofString(json.toString()))
                .header("api-key", apiKey)
                .header("Content-Type", "application/json")
                .build();

        final var result = HttpClient.newHttpClient().send(request, BodyHandlers.discarding());
        final var statusCode = result.statusCode();

        if (statusCode != 201) {
            throw new IOException("Unexpected response status: " + statusCode);
        }
    }
}