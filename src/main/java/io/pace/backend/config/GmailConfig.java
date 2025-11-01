package io.pace.backend.config;

import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.client.util.store.FileDataStoreFactory;
import com.google.api.services.gmail.Gmail;
import com.google.api.services.gmail.GmailScopes;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.StringReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

@Configuration
public class GmailConfig {

    private static final String APPLICATION_NAME = "pace_app_backend";
    private static final GsonFactory JSON_FACTORY = GsonFactory.getDefaultInstance();

    private static final List<String> SCOPES = List.of(
            GmailScopes.GMAIL_SEND,
            GmailScopes.GMAIL_READONLY
    );

    private static final Path TOKENS_PATH = Paths.get("tokens");

    @Bean
    public Gmail gmailClient() throws Exception {
        var httpTransport = GoogleNetHttpTransport.newTrustedTransport();
        var credential = authorize(httpTransport);

        return new Gmail.Builder(httpTransport, JSON_FACTORY, credential)
                .setApplicationName(APPLICATION_NAME)
                .build();
    }

    private Credential authorize(com.google.api.client.http.HttpTransport httpTransport) throws Exception {
        String secretJson = System.getenv("GMAIL_CLIENT_SECRET_JSON");

        if (secretJson == null || secretJson.isEmpty()) {
            throw new IllegalStateException("GMAIL_CLIENT_SECRET_JSON environment variable not set in Render");
        }

        GoogleClientSecrets clientSecrets = GoogleClientSecrets.load(JSON_FACTORY, new StringReader(secretJson));

        GoogleAuthorizationCodeFlow flow = new GoogleAuthorizationCodeFlow.Builder(
                httpTransport, JSON_FACTORY, clientSecrets, SCOPES)
                .setDataStoreFactory(new FileDataStoreFactory(TOKENS_PATH.toFile()))
                .setAccessType("offline") // ensures refresh token is stored
                .build();

        Credential credential = flow.loadCredential("user");

        // Auto-refresh access token if expired
        if (credential == null) {
            throw new IllegalStateException(
                    "No Gmail credentials found. Authorize locally using GmailAuth class and upload tokens.");
        } else if (credential.getExpiresInSeconds() != null && credential.getExpiresInSeconds() <= 60) {
            if (!credential.refreshToken()) {
                throw new IllegalStateException("Failed to refresh Gmail token. Re-run local auth to generate new token.");
            }
        }

        return credential;
    }
}
