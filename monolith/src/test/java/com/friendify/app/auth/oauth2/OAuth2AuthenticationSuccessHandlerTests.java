package com.friendify.app.auth.oauth2;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Map;
import java.util.Optional;
import java.util.Set;

import com.friendify.app.auth.entity.User;
import com.friendify.app.auth.enums.SignInProvider;
import com.friendify.app.auth.repository.UserRepository;
import com.friendify.app.auth.service.JwtService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.TestingAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.web.RedirectStrategy;
import org.springframework.test.util.ReflectionTestUtils;

class OAuth2AuthenticationSuccessHandlerTests {

    @Test
    void successHandlerCreatesOrLoadsUserAndRedirectsWithJwtToken() throws Exception {
        JwtService jwtService = mock(JwtService.class);
        UserRepository userRepository = mock(UserRepository.class);
        OAuth2Service oAuth2Service = mock(OAuth2Service.class);
        OAuth2AuthenticationSuccessHandler handler =
                new OAuth2AuthenticationSuccessHandler(jwtService, userRepository, oAuth2Service);
        RedirectStrategy redirectStrategy = mock(RedirectStrategy.class);
        handler.setRedirectStrategy(redirectStrategy);
        ReflectionTestUtils.setField(handler, "redirectUri", "http://localhost:5173/oauth2/redirect");

        User user = User.builder()
                .id("user-1")
                .email("alice@example.com")
                .username("alice")
                .build();
        when(oAuth2Service.getOrCreateUserFromOAuth(
                        "alice@example.com", "Alice Nguyen", "google-sub-1", SignInProvider.GOOGLE))
                .thenReturn(user);
        when(userRepository.findByIdWithRolesAndPermissions("user-1")).thenReturn(Optional.of(user));
        when(jwtService.generateToken(user)).thenReturn("jwt-token");

        DefaultOAuth2User principal = new DefaultOAuth2User(
                Set.of(new SimpleGrantedAuthority("ROLE_USER")),
                Map.of(
                        "sub", "google-sub-1",
                        "email", "alice@example.com",
                        "name", "Alice Nguyen"),
                "sub");
        TestingAuthenticationToken authentication = new TestingAuthenticationToken(principal, null);
        HttpServletRequest request = mock(HttpServletRequest.class);
        HttpServletResponse response = mock(HttpServletResponse.class);

        handler.onAuthenticationSuccess(request, response, authentication);

        verify(redirectStrategy).sendRedirect(
                request,
                response,
                "http://localhost:5173/oauth2/redirect?token=jwt-token");
    }
}
