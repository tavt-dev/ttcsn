package com.friendify.app.interaction.repository;

import java.util.Collection;
import java.util.Optional;

import com.friendify.app.interaction.entity.Like;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface LikeRepository extends JpaRepository<Like, String> {
    Optional<Like> findByUserIdAndPostIdAndCommentIdIsNull(String userId, String postId);

    Optional<Like> findByUserIdAndCommentIdAndPostIdIsNull(String userId, String commentId);

    Page<Like> findByPostIdAndCommentIdIsNull(String postId, Pageable pageable);

    Page<Like> findByCommentIdAndPostIdIsNull(String commentId, Pageable pageable);

    @Query("SELECT COUNT(l) FROM InteractionLike l WHERE l.postId = :postId AND l.commentId IS NULL")
    long countByPostId(@Param("postId") String postId);

    @Query("SELECT COUNT(l) FROM InteractionLike l WHERE l.commentId = :commentId AND l.postId IS NULL")
    long countByCommentId(@Param("commentId") String commentId);

    @Modifying
    @Query("DELETE FROM InteractionLike l WHERE l.postId = :postId")
    void deleteByPostId(@Param("postId") String postId);

    @Modifying
    @Query("DELETE FROM InteractionLike l WHERE l.commentId = :commentId")
    void deleteByCommentId(@Param("commentId") String commentId);

    @Modifying
    @Query("DELETE FROM InteractionLike l WHERE l.commentId IN :commentIds")
    void deleteByCommentIdIn(@Param("commentIds") Collection<String> commentIds);

    void deleteByUserId(String userId);
}
