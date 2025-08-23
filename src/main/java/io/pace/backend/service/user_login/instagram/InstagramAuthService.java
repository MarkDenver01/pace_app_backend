package io.pace.backend.service.user_login.instagram;

import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.Map;

@Service
public class InstagramAuthService {
    private static final String INSTAGRAM_USERINFO_URL =
            "https://graph.instagram.com/me?fields=id,username,account_type&access_token=";

    public Map<String, Object> verifyAccessToken(String accessToken) {
        RestTemplate restTemplate = new RestTemplate();
        String url = INSTAGRAM_USERINFO_URL + accessToken;

        ResponseEntity<Map> response = restTemplate.getForEntity(url, Map.class);
        Map<String, Object> userInfo = response.getBody();

        if (userInfo == null || !userInfo.containsKey("id")) {
            throw new RuntimeException("Invalid Instagram access token");
        }

        // Instagram does not directly return email.
        // You might need to request Basic Display API permissions.
        // We'll use "username@instagram.com" as fallback.
        userInfo.put("email", userInfo.get("username") + "@instagram.com");
        userInfo.put("name", userInfo.get("username"));

        return userInfo;
    }
}
