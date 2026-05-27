package com.friendify.app.auth.configuration;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;

import com.friendify.app.auth.entity.Role;
import com.friendify.app.auth.entity.User;
import com.friendify.app.auth.repository.RoleRepository;
import com.friendify.app.auth.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;

class ApplicationInitConfigTests {

    @Test
    void applicationRunnerSeedsDefaultRolesWithoutAdminByDefault() throws Exception {
        PasswordEncoder passwordEncoder = org.mockito.Mockito.mock(PasswordEncoder.class);
        UserRepository userRepository = org.mockito.Mockito.mock(UserRepository.class);
        RoleRepository roleRepository = org.mockito.Mockito.mock(RoleRepository.class);
        ApplicationInitConfig config = new ApplicationInitConfig(passwordEncoder);

        when(roleRepository.findById("USER")).thenReturn(Optional.empty());
        when(roleRepository.findById("ADMIN")).thenReturn(Optional.empty());
        when(roleRepository.save(any(Role.class))).thenAnswer(invocation -> invocation.getArgument(0));

        ApplicationRunner runner = config.applicationRunner(
                userRepository,
                roleRepository,
                false,
                "admin",
                "",
                "admin@friendify.local");

        runner.run(null);

        verify(roleRepository).save(argThat(role ->
                "USER".equals(role.getName()) && "User role".equals(role.getDescription())));
        verify(roleRepository).save(argThat(role ->
                "ADMIN".equals(role.getName()) && "Admin role".equals(role.getDescription())));
        verify(userRepository, never()).save(any(User.class));
    }
}
