package io.pace.backend.controller;

import com.google.api.client.auth.oauth2.TokenResponse;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.services.gmail.GmailScopes;
import io.pace.backend.domain.model.entity.GmailToken;
import io.pace.backend.repository.GmailTokenRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.StringReader;
import java.util.List;

@RestController
@RequestMapping("/api/gmail/oauth2")
public class GmailCallbackController {
    @Autowired
    private GmailTokenRepository gmailTokenRepository;

    @Value("${gmail.redirect.uri}")
    private String redirectUri;

    @Value("${GMAIL_CLIENT_SECRET_JSON}")
    private String clientSecretJson;

    @GetMapping("/callback")
    public ResponseEntity<?> callback(@RequestParam("code") String code) throws Exception {

        GoogleClientSecrets secrets = GoogleClientSecrets.load(
                GsonFactory.getDefaultInstance(),
                new StringReader(clientSecretJson)
        );

        GoogleAuthorizationCodeFlow flow = new GoogleAuthorizationCodeFlow.Builder(
                GoogleNetHttpTransport.newTrustedTransport(),
                GsonFactory.getDefaultInstance(),
                secrets,
                List.of(GmailScopes.GMAIL_SEND, GmailScopes.GMAIL_READONLY)
        )
                .setAccessType("offline")
                .setApprovalPrompt("force")
                .build();

        TokenResponse tokenResponse = flow.newTokenRequest(code)
                .setRedirectUri(redirectUri)
                .execute();

        GmailToken token = new GmailToken();
        token.setId(1L);
        token.setAccessToken(tokenResponse.getAccessToken());
        token.setRefreshToken(tokenResponse.getRefreshToken());
        token.setExpiresIn(tokenResponse.getExpiresInSeconds());
        token.setTokenCreatedAt(System.currentTimeMillis());

        gmailTokenRepository.save(token);

        return ResponseEntity.ok("Gmail successfully linked! You may close this tab.");
    }
}