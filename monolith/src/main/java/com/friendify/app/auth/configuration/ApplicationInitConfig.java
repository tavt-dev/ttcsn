package com.friendify.app.auth.configuration;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

import com.friendify.app.auth.entity.Role;
import com.friendify.app.auth.entity.User;
import com.friendify.app.auth.enums.PredefinedRole;
import com.friendify.app.auth.enums.SignInProvider;
import com.friendify.app.auth.repository.RoleRepository;
import com.friendify.app.auth.repository.UserRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.experimental.FieldDefaults;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.util.StringUtils;

@Configuration
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class ApplicationInitConfig {

    PasswordEncoder passwordEncoder;

    @Bean
    @ConditionalOnProperty(prefix = "friendify.seed", name = "enabled", havingValue = "true", matchIfMissing = true)
    ApplicationRunner applicationRunner(
            UserRepository userRepository,
            RoleRepository roleRepository,
            @Value("${friendify.seed.admin.enabled:false}") boolean seedAdminEnabled,
            @Value("${friendify.seed.admin.username:admin}") String adminUsername,
            @Value("${friendify.seed.admin.password:}") String adminPassword,
            @Value("${friendify.seed.admin.email:admin@friendify.local}") String adminEmail) {
        return args -> {
            Role userRole = ensureRole(roleRepository, PredefinedRole.USER_ROLE, "User role");
            Role adminRole = ensureRole(roleRepository, PredefinedRole.ADMIN_ROLE, "Admin role");

            if (seedAdminEnabled) {
                seedAdminUser(userRepository, adminUsername, adminPassword, adminEmail, adminRole);
            }

            log.info("[INIT] Default auth roles are ready: {}, {}", userRole.getName(), adminRole.getName());
        };
    }

    private Role ensureRole(RoleRepository roleRepository, String name, String description) {
        return roleRepository.findById(name)
                .orElseGet(() -> roleRepository.save(Role.builder()
                        .name(name)
                        .description(description)
                        .build()));
    }

    private void seedAdminUser(
            UserRepository userRepository,
            String adminUsername,
            String adminPassword,
            String adminEmail,
            Role adminRole) {
        if (!StringUtils.hasText(adminUsername)
                || !StringUtils.hasText(adminPassword)
                || !StringUtils.hasText(adminEmail)) {
            throw new IllegalStateException(
                    "Admin seed is enabled but username, password, or email is missing");
        }

        boolean adminExists = userRepository.findByUsername(adminUsername).isPresent()
                || userRepository.findByEmail(adminEmail).isPresent();
        if (adminExists) {
            log.info("[INIT] Admin seed skipped because username '{}' or email '{}' already exists",
                    adminUsername,
                    adminEmail);
            return;
        }

        LocalDateTime now = LocalDateTime.now();
        Set<Role> roles = new HashSet<>();
        roles.add(adminRole);

        User admin = User.builder()
                .username(adminUsername)
                .password(passwordEncoder.encode(adminPassword))
                .email(adminEmail)
                .emailVerified(true)
                .isActive(true)
                .provider(SignInProvider.LOCAL)
                .roles(roles)
                .createdAt(now)
                .updatedAt(now)
                .build();

        userRepository.save(admin);
        log.warn("[INIT] Admin '{}' was created from seed config. Change the password after first login.",
                adminUsername);
    }
}
