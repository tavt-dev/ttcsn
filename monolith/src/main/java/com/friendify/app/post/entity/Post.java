package com.friendify.app.post.entity;

import java.time.Instant;
import java.util.List;

import com.friendify.app.post.enums.PrivacyType;
import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
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
@Entity(name = "Post")
@Table(name = "post")
@FieldDefaults(level = AccessLevel.PRIVATE)
public class Post {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    String id;

    @Column(nullable = false)
    String userId;

    @Column(length = 5000)
    String content;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "post_image_urls", joinColumns = @JoinColumn(name = "post_id"))
    @Column(name = "image_url", length = 1000)
    List<String> imageUrls;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    PrivacyType privacy;

    String groupId;
    String originalPostId;
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
