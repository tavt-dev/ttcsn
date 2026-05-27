package com.friendify.app.auth.oauth2;

import java.util.HashSet;
import java.util.Random;

import com.friendify.app.auth.entity.Role;
import com.friendify.app.auth.entity.User;
import com.friendify.app.auth.enums.PredefinedRole;
import com.friendify.app.auth.enums.SignInProvider;
import com.friendify.app.auth.repository.RoleRepository;
import com.friendify.app.auth.repository.UserRepository;
import com.friendify.app.profile.dto.request.ProfileCreationRequest;
import com.friendify.app.profile.port.ProfileCreationPort;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class OAuth2Service {

    UserRepository userRepository;
    RoleRepository roleRepository;
    ProfileCreationPort profileCreationPort;
    Random random = new Random();

    @Transactional
    public User getOrCreateUserFromOAuth(String email, String name, String providerUserId, SignInProvider provider) {
        return userRepository.findByEmail(email)
                .map(user -> updateProviderIfNeeded(user, providerUserId, provider))
                .orElseGet(() -> createOAuthUser(email, name, providerUserId, provider));
    }

    private User updateProviderIfNeeded(User user, String providerUserId, SignInProvider provider) {
        if (user.getProvider() == null || !user.getProvider().equals(provider)) {
            user.setProvider(provider);
            user.setProviderUserId(providerUserId);
            user.setEmailVerified(true);
            user.setIsActive(true);
            return userRepository.save(user);
        }
        return user;
    }

    private User createOAuthUser(String email, String name, String providerUserId, SignInProvider provider) {
        String username = generateUniqueUsername(email);
        String[] nameParts = name != null && !name.isBlank() ? name.split(" ", 2) : new String[] {"", ""};
        String firstName = nameParts.length > 0 && !nameParts[0].isBlank() ? nameParts[0] : null;
        String lastName = nameParts.length > 1 && !nameParts[1].isBlank() ? nameParts[1] : username;

        User user = User.builder()
                .email(email)
                .username(username)
                .provider(provider)
                .providerUserId(providerUserId)
                .emailVerified(true)
                .isActive(true)
                .build();

        HashSet<Role> roles = new HashSet<>();
        roleRepository.findById(PredefinedRole.USER_ROLE).ifPresent(roles::add);
        user.setRoles(roles);

        user = userRepository.save(user);

        profileCreationPort.createProfile(ProfileCreationRequest.builder()
                .userId(user.getId())
                .username(username)
                .firstName(firstName)
                .lastName(lastName)
                .build());

        return user;
    }

    private String generateUniqueUsername(String email) {
        String baseUsername = email.split("@")[0];
        String username = baseUsername + "_" + System.currentTimeMillis();

        int attempts = 0;
        while (userRepository.existsByUsername(username) && attempts < 5) {
            username = baseUsername + "_" + System.currentTimeMillis() + "_" + random.nextInt(10000);
            attempts++;
        }

        return username;
    }
}
