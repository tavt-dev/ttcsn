package com.friendify.app.auth.oauth2;

import java.io.IOException;

import com.friendify.app.auth.entity.User;
import com.friendify.app.auth.enums.SignInProvider;
import com.friendify.app.auth.repository.UserRepository;
import com.friendify.app.auth.service.JwtService;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.experimental.NonFinal;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;

@Component
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class OAuth2AuthenticationSuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

    JwtService jwtService;
    UserRepository userRepository;
    OAuth2Service oAuth2Service;

    @NonFinal
    @Value("${app.oauth2.authorized-redirect-uri}")
    String redirectUri;

    @Override
    public void onAuthenticationSuccess(
            HttpServletRequest request, HttpServletResponse response, Authentication authentication)
            throws IOException, ServletException {
        OAuth2User principal = (OAuth2User) authentication.getPrincipal();
        String email = principal.getAttribute("email");
        String name = principal.getAttribute("name");
        String providerUserId = principal.getName();

        if (email == null || email.isBlank()) {
            String failUrl = UriComponentsBuilder.fromUriString(redirectUri)
                    .queryParam("error", "EMAIL_NOT_PROVIDED")
                    .build()
                    .toUriString();
            getRedirectStrategy().sendRedirect(request, response, failUrl);
            return;
        }

        User user = oAuth2Service.getOrCreateUserFromOAuth(email, name, providerUserId, SignInProvider.GOOGLE);
        User userWithRoles = userRepository.findByIdWithRolesAndPermissions(user.getId()).orElse(user);
        String token = jwtService.generateToken(userWithRoles);

        String targetUrl = UriComponentsBuilder.fromUriString(redirectUri)
                .queryParam("token", token)
                .build()
                .toUriString();
        getRedirectStrategy().sendRedirect(request, response, targetUrl);
    }
}
