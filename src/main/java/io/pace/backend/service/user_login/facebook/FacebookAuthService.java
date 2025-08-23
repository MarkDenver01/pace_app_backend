package io.pace.backend.service.user_login.facebook;

import io.pace.backend.config.FacebookConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.server.ResponseStatusException;

import java.util.Map;

@Service
public class FacebookAuthService {

    @Autowired
    FacebookConfig facebookConfig;

    private final RestTemplate restTemplate = new RestTemplate();

    public Map<String, Object> verifyAccessToken(String accessToken) {
        String debugUrl = String.format(
                "https://graph.facebook.com/debug_token?input_token=%s&access_token=%s|%s",
                accessToken, facebookConfig.getFacebookAppId(), facebookConfig.getFacebookAppSecret()
        );

        Map<String, Object> debugResponse = restTemplate.getForObject(debugUrl, Map.class);
        Map<String, Object> data = (Map<String, Object>) debugResponse.get("data");

        if (data == null || !(Boolean) data.get("is_valid")) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid Facebook access token");
        }

        return getUserProfile(accessToken);
    }

    private Map<String, Object> getUserProfile(String accessToken) {
        String userUrl = String.format(
                "https://graph.facebook.com/me?fields=id,name,email&access_token=%s",
                accessToken
        );

        return restTemplate.getForObject(userUrl, Map.class);
    }
}
