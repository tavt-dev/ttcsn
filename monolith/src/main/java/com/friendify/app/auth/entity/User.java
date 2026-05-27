package com.friendify.app.auth.entity;

import java.time.LocalDateTime;
import java.util.Set;

import com.friendify.app.auth.enums.SignInProvider;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.FieldDefaults;

@Entity
@Setter
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    String id;

    @Column(name = "username", unique = true, nullable = false)
    String username;

    @Column(name = "password")
    String password;

    @Column(name = "email", unique = true, nullable = false)
    String email;

    @Builder.Default
    @Column(name = "email_verified", nullable = false)
    boolean emailVerified = false;

    @Enumerated(EnumType.STRING)
    SignInProvider provider;

    @Builder.Default
    Boolean isActive = false;

    @Column(length = 128)
    String providerUserId;

    LocalDateTime createdAt;

    LocalDateTime updatedAt;

    @PrePersist
    void prePersist() {
        if (email != null) {
            email = email.trim().toLowerCase();
        }
        createdAt = LocalDateTime.now();
        updatedAt = createdAt;
    }

    @PreUpdate
    void preUpdate() {
        if (email != null) {
            email = email.trim().toLowerCase();
        }
        updatedAt = LocalDateTime.now();
    }

    @ManyToMany(fetch = FetchType.LAZY)
    Set<Role> roles;
}
