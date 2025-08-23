package io.pace.backend.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Configuration
public class FacebookConfig {
    @Value("${spring.security.oauth2.client.registration.facebook.client-id}")
    private String facebookAppId;

    @Value("${spring.security.oauth2.client.registration.facebook.client-secret}")
    private String facebookAppSecret;

    public String getFacebookAppId() {
        return facebookAppId;
    }

    public String getFacebookAppSecret() {
        return facebookAppSecret;
    }

    public String getAppAccessToken() {
        return facebookAppId + "|" + facebookAppSecret;
    }
}
