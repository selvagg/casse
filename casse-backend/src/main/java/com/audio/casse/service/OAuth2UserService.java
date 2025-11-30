package com.audio.casse.service;

import com.audio.casse.models.AuthProvider;
import com.audio.casse.models.LoginUserDetails;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class OAuth2UserService extends DefaultOAuth2UserService {

    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        OAuth2User oAuth2User = super.loadUser(userRequest);
        Map<String, Object> attributes = oAuth2User.getAttributes();
        String email = (String) attributes.get("email");
        String name = (String) attributes.get("name");

        if (userRequest.getClientRegistration().getRegistrationId().equalsIgnoreCase(AuthProvider.INSTAGRAM.name())) {
            name = (String) attributes.get("username");
        }

        return LoginUserDetails.builder()
                .email(email)
                .firstName(name)
                .attributes(attributes)
                .build();
    }
}
