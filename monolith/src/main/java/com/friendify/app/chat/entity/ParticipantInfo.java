package com.friendify.app.chat.entity;

import com.friendify.app.chat.enums.ParticipantRole;
import jakarta.persistence.Embeddable;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.FieldDefaults;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Embeddable
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ParticipantInfo {
    String userId;
    String username;
    String firstName;
    String lastName;
    String avatar;

    @Builder.Default
    @Enumerated(EnumType.STRING)
    ParticipantRole role = ParticipantRole.MEMBER;
}
