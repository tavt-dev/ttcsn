package com.friendify.app.auth.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;

import com.friendify.app.auth.entity.InvalidatedToken;
import com.friendify.app.auth.entity.User;
import com.friendify.app.auth.repository.InvalidatedTokenRepository;
import com.friendify.app.auth.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.test.util.ReflectionTestUtils;

class JwtServiceTests {

    @Test
    void generatedTokenIsValidAndCanBeRevoked() {
        UserRepository userRepository = org.mockito.Mockito.mock(UserRepository.class);
        InvalidatedTokenRepository invalidatedTokenRepository =
                org.mockito.Mockito.mock(InvalidatedTokenRepository.class);
        JwtService jwtService = new JwtService(userRepository, invalidatedTokenRepository);
        User user = User.builder()
                .id("user-1")
                .username("alice")
                .email("alice@example.com")
                .build();

        ReflectionTestUtils.setField(
                jwtService,
                "signerKey",
                "0123456789abcdef0123456789abcdef0123456789abcdef0123456789abcdef");
        ReflectionTestUtils.setField(jwtService, "issuer", "friendify.com");
        ReflectionTestUtils.setField(jwtService, "validDuration", 3600L);
        ReflectionTestUtils.setField(jwtService, "refreshableDuration", 36000L);

        when(userRepository.findByIdWithRolesAndPermissions("user-1")).thenReturn(Optional.of(user));
        when(invalidatedTokenRepository.existsById(anyString())).thenReturn(false);

        String token = jwtService.generateToken(user);

        assertThat(jwtService.isValidToken(token)).isTrue();
        jwtService.revokeToken(token);

        ArgumentCaptor<InvalidatedToken> tokenCaptor = ArgumentCaptor.forClass(InvalidatedToken.class);
        verify(invalidatedTokenRepository).save(tokenCaptor.capture());
        assertThat(tokenCaptor.getValue().getId()).isNotBlank();
        assertThat(tokenCaptor.getValue().getExpiryTime()).isNotNull();
    }
}
