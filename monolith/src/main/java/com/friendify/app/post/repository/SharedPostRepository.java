package com.friendify.app.post.repository;

import java.util.Optional;

import com.friendify.app.post.entity.SharedPost;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
public interface SharedPostRepository extends JpaRepository<SharedPost, String> {
    Page<SharedPost> findAllByPostId(String postId, Pageable pageable);

    long countByPostId(String postId);

    @Transactional
    @Modifying
    @Query("DELETE FROM SharedPost s WHERE s.postId = :postId")
    void deleteAllByPostId(@Param("postId") String postId);

    Optional<SharedPost> findByUserIdAndPostId(String userId, String postId);
}
