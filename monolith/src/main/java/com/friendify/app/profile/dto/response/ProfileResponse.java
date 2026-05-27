package com.friendify.app.profile.dto.response;

import java.time.LocalDate;
import java.time.LocalDateTime;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ProfileResponse {
    String id;
    String userId;
    String username;
    String firstName;
    String lastName;
    LocalDate dob;
    String city;
    String country;
    String bio;
    String phoneNumber;
    String gender;
    String website;
    String avatar;
    String backgroundImage;
    LocalDateTime createdAt;
    LocalDateTime updatedAt;
}
