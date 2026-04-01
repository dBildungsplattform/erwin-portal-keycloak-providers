package com.spsh.ldap;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpRequest.BodyPublishers;
import java.net.http.HttpResponse.BodyHandlers;

import com.spsh.ldap.dto.LdapImportData;

import com.spsh.util.ApiFetchHelper;

public final class LdapUserRequestHelper {

    private LdapUserRequestHelper() {
        throw new UnsupportedOperationException();
    }

    public static void sendLdapUserData(URI uri, LdapImportData ldapImportData) throws IOException, InterruptedException {
        final var apiKey = System.getenv(ApiFetchHelper.ENV_KEY_INTERNAL_COMMUNICATION_API_KEY);
        if (apiKey == null || apiKey.isEmpty()) {
            throw new IOException(String.format("Environment variable %s is not set or is empty.",
                    ApiFetchHelper.ENV_KEY_INTERNAL_COMMUNICATION_API_KEY));
        }

        final var jsonBody = ldapImportData.toJson().toString();
        final var request = HttpRequest.newBuilder(uri)
                .POST(BodyPublishers.ofString(jsonBody))
                .header("api-key", apiKey)
                .header("Content-Type", "application/json")
                .build();

        @SuppressWarnings("resource") // Only try-with-resource compatible as of JDK 21
        final var httpClient = HttpClient.newHttpClient();
        final var result = httpClient.send(request, BodyHandlers.discarding());
        final var statusCode = result.statusCode();
        if (statusCode != 201) {
            throw new IOException("Unexpected response status: " + statusCode);
        }

    }
}
