package com.friendify.app.auth.oauth2;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.startsWith;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;

import com.friendify.app.auth.entity.User;
import com.friendify.app.auth.enums.SignInProvider;
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

@ExtendWith(MockitoExtension.class)
class OAuth2ServiceTests {

    @Mock
    UserRepository userRepository;

    @Mock
    RoleRepository roleRepository;

    @Mock
    ProfileCreationPort profileCreationPort;

    @InjectMocks
    OAuth2Service oAuth2Service;

    @Test
    void newOAuthUserCreatesProfileThroughPort() {
        when(userRepository.findByEmail("alice@example.com")).thenReturn(Optional.empty());
        when(roleRepository.findById("USER")).thenReturn(Optional.empty());
        when(userRepository.existsByUsername(startsWith("alice_"))).thenReturn(false);
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> {
            User user = invocation.getArgument(0);
            user.setId("user-1");
            return user;
        });

        User user = oAuth2Service.getOrCreateUserFromOAuth(
                "alice@example.com",
                "Alice Nguyen",
                "google-sub-1",
                SignInProvider.GOOGLE);

        ArgumentCaptor<ProfileCreationRequest> profileRequestCaptor =
                ArgumentCaptor.forClass(ProfileCreationRequest.class);
        verify(profileCreationPort).createProfile(profileRequestCaptor.capture());
        ProfileCreationRequest profileRequest = profileRequestCaptor.getValue();

        assertThat(user.getId()).isEqualTo("user-1");
        assertThat(user.getEmail()).isEqualTo("alice@example.com");
        assertThat(user.getProvider()).isEqualTo(SignInProvider.GOOGLE);
        assertThat(user.isEmailVerified()).isTrue();
        assertThat(user.getIsActive()).isTrue();
        assertThat(profileRequest.getUserId()).isEqualTo("user-1");
        assertThat(profileRequest.getUsername()).startsWith("alice_");
        assertThat(profileRequest.getFirstName()).isEqualTo("Alice");
        assertThat(profileRequest.getLastName()).isEqualTo("Nguyen");
    }
}
