package com.friendify.app.post.entity;

import java.time.Instant;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
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
@Entity
@Table(name = "shared_posts")
@FieldDefaults(level = AccessLevel.PRIVATE)
public class SharedPost {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    String id;

    @Column(nullable = false)
    String userId;

    @Column(nullable = false)
    String postId;

    String originalPostUserId;

    @Column(length = 5000)
    String content;

    Instant sharedDate;

    @PrePersist
    void prePersist() {
        sharedDate = sharedDate == null ? Instant.now() : sharedDate;
    }
}
