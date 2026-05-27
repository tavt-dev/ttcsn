package com.friendify.app.auth.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.contains;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;

import com.friendify.app.auth.dto.request.UserCreationRequest;
import com.friendify.app.auth.dto.response.UserResponse;
import com.friendify.app.auth.entity.User;
import com.friendify.app.auth.entity.UserOtp;
import com.friendify.app.auth.enums.OtpType;
import com.friendify.app.auth.mapper.UserMapper;
import com.friendify.app.auth.port.NotificationDeliveryPort;
import com.friendify.app.auth.repository.RoleRepository;
import com.friendify.app.auth.repository.UserRepository;
import com.friendify.app.profile.dto.request.ProfileCreationRequest;
import com.friendify.app.profile.port.ProfileCreationPort;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

@ExtendWith(MockitoExtension.class)
class AuthenticationServiceTests {

    @Mock
    UserRepository userRepository;

    @Mock
    UserMapper userMapper;

    @Mock
    PasswordEncoder passwordEncoder;

    @Mock
    RoleRepository roleRepository;

    @Mock
    JwtService jwtService;

    @Mock
    OtpService otpService;

    @Mock
    NotificationDeliveryPort notificationDeliveryPort;

    @Mock
    ProfileCreationPort profileCreationPort;

    @InjectMocks
    AuthenticationService authenticationService;

    @Test
    void registerCreatesProfileThroughPortAndSendsVerificationEmail() {
        UserCreationRequest request = UserCreationRequest.builder()
                .username("alice")
                .password("secret123")
                .email("alice@example.com")
                .firstName("Alice")
                .build();
        User mappedUser = User.builder()
                .username("alice")
                .email("alice@example.com")
                .build();
        User savedUser = User.builder()
                .id("user-1")
                .username("alice")
                .email("alice@example.com")
                .build();
        UserOtp otp = UserOtp.builder()
                .user(savedUser)
                .type(OtpType.REGISTER)
                .otpCode("123456")
                .build();
        UserResponse mappedResponse = UserResponse.builder()
                .username("alice")
                .email("alice@example.com")
                .build();

        when(userMapper.toUser(request)).thenReturn(mappedUser);
        when(passwordEncoder.encode("secret123")).thenReturn("encoded");
        when(roleRepository.findById("USER")).thenReturn(Optional.empty());
        when(userRepository.save(mappedUser)).thenReturn(savedUser);
        when(otpService.createOtp(savedUser, OtpType.REGISTER, 15)).thenReturn(otp);
        when(userMapper.toUserResponse(savedUser)).thenReturn(mappedResponse);

        UserResponse response = authenticationService.register(request);

        ArgumentCaptor<ProfileCreationRequest> profileRequestCaptor =
                ArgumentCaptor.forClass(ProfileCreationRequest.class);
        verify(profileCreationPort).createProfile(profileRequestCaptor.capture());
        ProfileCreationRequest profileRequest = profileRequestCaptor.getValue();
        assertThat(profileRequest.getUserId()).isEqualTo("user-1");
        assertThat(profileRequest.getUsername()).isEqualTo("alice");
        assertThat(profileRequest.getFirstName()).isEqualTo("Alice");
        assertThat(profileRequest.getLastName()).isEqualTo("alice");
        assertThat(response.getId()).isEqualTo("user-1");
        verify(notificationDeliveryPort)
                .sendEmail(eq("alice@example.com"), eq("Verify email"), contains("123456"));
        verify(userRepository).save(mappedUser);
    }
}
