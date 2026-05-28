package com.friendify.app.group.entity;

import java.time.Instant;

import com.friendify.app.group.enums.GroupPrivacy;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;

@Entity(name = "FriendifyGroup")
@Table(name = "`group`")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class Group {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    String id;

    @Column(nullable = false)
    String name;

    @Column(length = 2000)
    String description;

    @Column(length = 1000)
    String coverImageUrl;

    @Column(length = 1000)
    String avatarUrl;

    @Column(nullable = false)
    String ownerId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    GroupPrivacy privacy;

    boolean requiresApproval;
    boolean allowPosting;
    boolean moderationRequired;
    boolean onlyAdminCanPost;
    Instant createdDate;
    Instant modifiedDate;

    @PrePersist
    void prePersist() {
        Instant now = Instant.now();
        createdDate = createdDate == null ? now : createdDate;
        modifiedDate = modifiedDate == null ? now : modifiedDate;
    }

    @PreUpdate
    void preUpdate() {
        modifiedDate = Instant.now();
    }
}
