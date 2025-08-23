package io.pace.backend.service.user_login.twitter;

import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;

@Service
public class TwitterAuthService {

    private static final String TWITTER_USERINFO_URL =
            "https://api.twitter.com/2/users/me?user.fields=id,name,username";

    public Map<String, Object> verifyAccessToken(String accessToken) {
        RestTemplate restTemplate = new RestTemplate();

        // Twitter v2 API requires Bearer token in Authorization header
        org.springframework.http.HttpHeaders headers = new org.springframework.http.HttpHeaders();
        headers.set("Authorization", "Bearer " + accessToken);

        org.springframework.http.HttpEntity<String> entity =
                new org.springframework.http.HttpEntity<>(headers);

        ResponseEntity<Map> response = restTemplate.exchange(
                TWITTER_USERINFO_URL,
                org.springframework.http.HttpMethod.GET,
                entity,
                Map.class
        );

        Map<String, Object> userInfo = response.getBody();
        if (userInfo == null || !userInfo.containsKey("data")) {
            throw new RuntimeException("Invalid Twitter access token");
        }

        Map<String, Object> data = (Map<String, Object>) userInfo.get("data");
        userInfo.put("email", data.get("username") + "@twitter.com"); // fallback
        userInfo.put("name", data.get("name"));

        return userInfo;
    }
}
