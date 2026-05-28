package com.friendify.app.file.repository;

import java.util.Optional;

import com.friendify.app.file.entity.Image;
import com.friendify.app.shared.media.ImageType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ImageRepository extends JpaRepository<Image, String> {
    Optional<Image> findByPublicId(String publicId);

    Optional<Image> findByOwnerIdAndImageType(String ownerId, ImageType imageType);

    Optional<Image> findByPostId(String postId);
}
