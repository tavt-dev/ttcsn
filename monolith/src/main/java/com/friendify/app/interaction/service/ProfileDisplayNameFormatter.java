package com.friendify.app.interaction.service;

import com.friendify.app.profile.dto.response.ProfileResponse;
import org.springframework.stereotype.Component;

@Component
public class ProfileDisplayNameFormatter {

    public String displayName(ProfileResponse profile) {
        if (profile == null) {
            return null;
        }

        String firstName = profile.getFirstName();
        String lastName = profile.getLastName();
        String username = profile.getUsername();

        if (hasText(firstName) && hasText(lastName)) {
            return (firstName.trim() + " " + lastName.trim()).trim();
        }
        if (hasText(lastName)) {
            return lastName.trim();
        }
        if (hasText(firstName)) {
            return firstName.trim();
        }
        return username != null ? username : "";
    }

    private boolean hasText(String value) {
        return value != null && !value.trim().isEmpty();
    }
}
