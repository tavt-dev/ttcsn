package com.friendify.app.interaction.service;

import java.util.List;

import com.friendify.app.interaction.port.InteractionCleanupPort;
import com.friendify.app.interaction.port.InteractionQueryPort;
import com.friendify.app.interaction.repository.CommentRepository;
import com.friendify.app.interaction.repository.LikeRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class InteractionPortService implements InteractionQueryPort, InteractionCleanupPort {
    LikeRepository likeRepository;
    CommentRepository commentRepository;

    @Override
    public long countLikesByPostId(String postId) {
        return likeRepository.countByPostId(postId);
    }

    @Override
    public long countCommentsByPostId(String postId) {
        return commentRepository.countByPostId(postId);
    }

    @Override
    public boolean isLikedByCurrentUser(String postId, String userId) {
        return likeRepository.findByUserIdAndPostIdAndCommentIdIsNull(userId, postId).isPresent();
    }

    @Override
    @Transactional
    public void deleteByPostId(String postId) {
        List<String> commentIds = commentRepository.findIdsByPostId(postId);
        if (!commentIds.isEmpty()) {
            likeRepository.deleteByCommentIdIn(commentIds);
        }
        commentRepository.deleteByPostId(postId);
        likeRepository.deleteByPostId(postId);
    }
}
