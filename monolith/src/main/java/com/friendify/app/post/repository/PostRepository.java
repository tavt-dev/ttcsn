package com.friendify.app.post.repository;

import java.util.List;

import com.friendify.app.post.entity.Post;
import com.friendify.app.post.enums.PrivacyType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
public interface PostRepository extends JpaRepository<Post, String> {
    Page<Post> findAllByUserId(String userId, Pageable pageable);

    Page<Post> findByPrivacyAndUserIdNotIn(PrivacyType privacy, List<String> userIds, Pageable pageable);

    @Query("""
            SELECT p FROM Post p
            WHERE p.userId IN :userIds
              AND (p.privacy = com.friendify.app.post.enums.PrivacyType.PUBLIC
                   OR (p.privacy = com.friendify.app.post.enums.PrivacyType.PRIVATE AND p.userId = :currentUserId))
            """)
    Page<Post> findByUserIdInWithPrivacyFilter(
            @Param("userIds") List<String> userIds, @Param("currentUserId") String currentUserId, Pageable pageable);

    @Query("""
            SELECT p FROM Post p
            WHERE p.userId = :userId
              AND (p.privacy = com.friendify.app.post.enums.PrivacyType.PUBLIC
                   OR p.privacy = com.friendify.app.post.enums.PrivacyType.PRIVATE)
            """)
    Page<Post> findByUserIdWithPrivacy(@Param("userId") String userId, Pageable pageable);

    @Query("""
            SELECT p FROM Post p
            WHERE (p.privacy = com.friendify.app.post.enums.PrivacyType.PUBLIC OR p.userId = :currentUserId)
              AND LOWER(COALESCE(p.content, '')) LIKE LOWER(CONCAT('%', :keyword, '%'))
              AND p.userId NOT IN :excludedUserIds
              AND p.groupId IS NULL
            """)
    Page<Post> searchPublicPosts(
            @Param("currentUserId") String currentUserId,
            @Param("keyword") String keyword,
            @Param("excludedUserIds") List<String> excludedUserIds,
            Pageable pageable);

    @Query("""
            SELECT p FROM Post p
            WHERE p.groupId = :groupId
              AND (p.privacy = com.friendify.app.post.enums.PrivacyType.PUBLIC OR p.userId = :currentUserId)
            """)
    Page<Post> findByGroupIdWithPrivacy(
            @Param("groupId") String groupId, @Param("currentUserId") String currentUserId, Pageable pageable);

    Page<Post> findByUserIdAndOriginalPostIdIsNotNull(String userId, Pageable pageable);

    List<Post> findAllByOriginalPostId(String originalPostId);

    @Transactional
    @Modifying
    @Query("DELETE FROM Post p WHERE p.originalPostId = :originalPostId")
    void deleteAllByOriginalPostId(@Param("originalPostId") String originalPostId);
}
