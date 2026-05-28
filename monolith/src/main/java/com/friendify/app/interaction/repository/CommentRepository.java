package com.friendify.app.interaction.repository;

import java.util.List;
import java.util.Optional;

import com.friendify.app.interaction.entity.Comment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface CommentRepository extends JpaRepository<Comment, String> {
    Page<Comment> findByPostIdAndParentCommentIdIsNull(String postId, Pageable pageable);

    List<Comment> findByParentCommentIdOrderByCreatedAtAsc(String parentCommentId);

    List<Comment> findByParentCommentIdInOrderByCreatedAtAsc(List<String> parentCommentIds);

    Optional<Comment> findByIdAndUserId(String id, String userId);

    @Query("SELECT COUNT(c) FROM InteractionComment c WHERE c.postId = :postId")
    long countByPostId(@Param("postId") String postId);

    @Query("SELECT c.id FROM InteractionComment c WHERE c.postId = :postId")
    List<String> findIdsByPostId(@Param("postId") String postId);

    void deleteByPostId(String postId);

    void deleteByUserId(String userId);
}
