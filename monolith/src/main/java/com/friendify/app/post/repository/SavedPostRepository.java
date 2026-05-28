package com.friendify.app.post.repository;

import java.util.Optional;

import com.friendify.app.post.entity.SavedPost;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
public interface SavedPostRepository extends JpaRepository<SavedPost, String> {
    Optional<SavedPost> findByUserIdAndPostId(String userId, String postId);

    Page<SavedPost> findAllByUserId(String userId, Pageable pageable);

    boolean existsByUserIdAndPostId(String userId, String postId);

    @Transactional
    @Modifying
    @Query("DELETE FROM SavedPost s WHERE s.postId = :postId")
    void deleteAllByPostId(@Param("postId") String postId);
}
