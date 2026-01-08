package com.ie207.vagabond.service;

import com.ie207.vagabond.response.GoogleTokenResponse;
import com.ie207.vagabond.response.GoogleUserInfo;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

@Service
@RequiredArgsConstructor
public class GoogleAuthService {
    @Value("${spring.security.oauth2.client.registration.google.client-id}")
    private String clientId;

    @Value("${spring.security.oauth2.client.registration.google.client-secret}")
    private String clientSecret;

    @Value("${spring.security.oauth2.client.registration.google.redirect-uri}")
    private String redirectUri;

    private final RestTemplate restTemplate = new RestTemplate();

    public GoogleUserInfo getUserInfo(String authCode) {
        String tokenEndpoint = "https://oauth2.googleapis.com/token";

        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("code", authCode);
        params.add("client_id", clientId);
        params.add("client_secret", clientSecret);
        params.add("redirect_uri", redirectUri);
        params.add("grant_type", "authorization_code");
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(params, headers);

        ResponseEntity<GoogleTokenResponse> tokenResponse = restTemplate.postForEntity(
                tokenEndpoint, request, GoogleTokenResponse.class
        );

        String accessToken = tokenResponse.getBody().getAccessToken();
        String userInfoEndpoint = "https://www.googleapis.com/oauth2/v2/userinfo";
        HttpHeaders headersUser = new HttpHeaders();
        headersUser.setBearerAuth(accessToken);
        HttpEntity<String> entity = new HttpEntity<>(headersUser);

        ResponseEntity<GoogleUserInfo> userInfoResponse = restTemplate.exchange(
                userInfoEndpoint, HttpMethod.GET, entity, GoogleUserInfo.class
        );

        return userInfoResponse.getBody();
    }



}
