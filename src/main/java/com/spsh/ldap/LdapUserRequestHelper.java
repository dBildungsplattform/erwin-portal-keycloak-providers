package com.spsh.ldap;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpRequest.BodyPublishers;
import java.net.http.HttpResponse.BodyHandlers;

import org.json.JSONObject;

import com.spsh.util.ApiFetchHelper;

public final class LdapUserRequestHelper {

    private LdapUserRequestHelper() {
        throw new UnsupportedOperationException();
    }
    
    public static void sendLdapUserData(URI uri, String keycloakUserId, String userName, String email,
            String firstName,
            String lastName, String ldapDn, String ldapId) throws IOException, InterruptedException {
        final var apiKey = System.getenv(ApiFetchHelper.ENV_KEY_INTERNAL_COMMUNICATION_API_KEY);
        if (apiKey == null || apiKey.isEmpty()) {
            throw new IOException(String.format("Environment variable %s is not set or is empty.",
                    ApiFetchHelper.ENV_KEY_INTERNAL_COMMUNICATION_API_KEY));
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
