package com.friendify.app.post.entity;

import java.time.Instant;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
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
@Table(
        name = "saved_posts",
        uniqueConstraints = @UniqueConstraint(name = "uk_saved_posts_user_post", columnNames = {"user_id", "post_id"}))
@FieldDefaults(level = AccessLevel.PRIVATE)
public class SavedPost {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    String id;

    @Column(name = "user_id", nullable = false)
    String userId;

    @Column(name = "post_id", nullable = false)
    String postId;

    Instant savedDate;

    @PrePersist
    void prePersist() {
        savedDate = savedDate == null ? Instant.now() : savedDate;
    }
}
