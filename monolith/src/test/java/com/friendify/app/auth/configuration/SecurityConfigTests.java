package com.friendify.app.auth.configuration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

import com.friendify.app.auth.oauth2.OAuth2AuthenticationFailureHandler;
import com.friendify.app.auth.oauth2.OAuth2AuthenticationSuccessHandler;
import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.security.oauth2.server.resource.web.BearerTokenResolver;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.mock.web.MockHttpServletRequest;

class SecurityConfigTests {

    @Test
    void bearerTokenResolverUsesAuthorizationHeaderBeforeOAuthCookie() {
        SecurityConfig securityConfig = securityConfig();
        BearerTokenResolver resolver = securityConfig.bearerTokenResolver();
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader(HttpHeaders.AUTHORIZATION, "Bearer header-token");
        request.setCookies(new Cookie("FRIENDIFY_ACCESS_TOKEN", "cookie-token"));

        assertThat(resolver.resolve(request)).isEqualTo("header-token");
    }

    @Test
    void bearerTokenResolverReadsOAuthTokenCookieWhenAuthorizationHeaderIsMissing() {
        SecurityConfig securityConfig = securityConfig();
        BearerTokenResolver resolver = securityConfig.bearerTokenResolver();
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setCookies(new Cookie("FRIENDIFY_ACCESS_TOKEN", "cookie-token"));

        assertThat(resolver.resolve(request)).isEqualTo("cookie-token");
    }

    private SecurityConfig securityConfig() {
        SecurityConfig securityConfig = new SecurityConfig(
                mock(CustomJwtDecoder.class),
                mock(JwtAuthenticationEntryPoint.class),
                mock(OAuth2AuthenticationSuccessHandler.class),
                mock(OAuth2AuthenticationFailureHandler.class));
        ReflectionTestUtils.setField(securityConfig, "accessTokenCookieName", "FRIENDIFY_ACCESS_TOKEN");
        return securityConfig;
    }
}
