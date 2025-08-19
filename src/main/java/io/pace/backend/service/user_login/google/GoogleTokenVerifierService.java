package io.pace.backend.service.user_login.google;

import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import io.pace.backend.config.GoogleIdTokenVerifierConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class GoogleTokenVerifierService {

    @Autowired
    GoogleIdTokenVerifierConfig verifierConfig;

    public GoogleIdToken.Payload verify(String idToken) throws Exception {
        GoogleIdToken googleIdToken = verifierConfig.googleIdTokenVerifier().verify(idToken);
        if (googleIdToken != null) {
            return googleIdToken.getPayload();
        }
        throw new RuntimeException("Could not verify Google IdToken");
    }

}
