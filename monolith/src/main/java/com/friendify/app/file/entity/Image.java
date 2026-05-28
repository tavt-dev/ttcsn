package com.friendify.app.file.entity;

import java.time.LocalDateTime;

import com.friendify.app.shared.media.ImageType;
import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
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
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.FieldDefaults;

@Setter
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "file")
@FieldDefaults(level = AccessLevel.PRIVATE)
public class Image {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    String id;

    String ownerId;
    String postId;

    String contentType;
    String format;
    Integer width;
    Integer height;
    Long size;

    @Enumerated(EnumType.STRING)
    ImageType imageType;

    @Column(length = 1000)
    String secureUrl;

    @Column(unique = true)
    String publicId;
    String forder;

    String version;
    String checksum;

    LocalDateTime createdDate;

    LocalDateTime updatedAt;

    @Embedded
    ImageVersions imageVersions;

    @PrePersist
    void prePersist() {
        createdDate = LocalDateTime.now();
        updatedAt = createdDate;
    }

    @PreUpdate
    void preUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
