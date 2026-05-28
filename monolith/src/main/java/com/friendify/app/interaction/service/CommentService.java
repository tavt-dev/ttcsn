package com.friendify.app.interaction.service;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import com.friendify.app.interaction.dto.request.CreateCommentRequest;
import com.friendify.app.interaction.dto.request.UpdateCommentRequest;
import com.friendify.app.interaction.dto.response.CommentResponse;
import com.friendify.app.interaction.entity.Comment;
import com.friendify.app.interaction.mapper.CommentMapper;
import com.friendify.app.interaction.port.PostQueryPort;
import com.friendify.app.interaction.repository.CommentRepository;
import com.friendify.app.interaction.repository.LikeRepository;
import com.friendify.app.profile.dto.response.ProfileResponse;
import com.friendify.app.profile.port.ProfileQueryPort;
import com.friendify.app.shared.dto.PageResponse;
import com.friendify.app.shared.exception.AppException;
import com.friendify.app.shared.exception.ErrorCode;
import com.friendify.app.shared.security.CurrentUserProvider;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.experimental.FieldDefaults;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class CommentService {
    CommentRepository commentRepository;
    LikeRepository likeRepository;
    PostQueryPort postQueryPort;
    ProfileQueryPort profileQueryPort;
    CommentMapper commentMapper;
    CurrentUserProvider currentUserProvider;
    ProfileDisplayNameFormatter displayNameFormatter;

    @Transactional
    public CommentResponse createComment(CreateCommentRequest request) {
        String userId = currentUserProvider.getCurrentUserId();
        validatePostExists(request.getPostId());

        if (request.getParentCommentId() != null && !request.getParentCommentId().isEmpty()) {
            Comment parent = commentRepository.findById(request.getParentCommentId())
                    .orElseThrow(() -> new AppException(ErrorCode.COMMENT_NOT_FOUND));
            if (!parent.getPostId().equals(request.getPostId())) {
                throw new AppException(ErrorCode.INVALID_PARENT_COMMENT);
            }
        }

        Comment comment = commentRepository.save(Comment.builder()
                .postId(request.getPostId())
                .userId(userId)
                .content(request.getContent())
                .parentCommentId(request.getParentCommentId())
                .build());

        return buildCommentResponse(comment, userId);
    }

    public PageResponse<CommentResponse> getCommentsByPost(String postId, int page, int size) {
        String userId = currentUserProvider.getCurrentUserId();
        Pageable pageable = PageRequest.of(page - 1, size, Sort.by("createdAt").descending());
        Page<Comment> commentsPage = commentRepository.findByPostIdAndParentCommentIdIsNull(postId, pageable);

        List<CommentResponse> responses = commentsPage.getContent().stream()
                .map(comment -> buildCommentResponse(comment, userId))
                .collect(Collectors.toList());

        if (!responses.isEmpty()) {
            List<String> commentIds = responses.stream()
                    .map(CommentResponse::getId)
                    .toList();
            var repliesMap = commentRepository.findByParentCommentIdInOrderByCreatedAtAsc(commentIds).stream()
                    .collect(Collectors.groupingBy(Comment::getParentCommentId));

            responses.forEach(response -> {
                List<CommentResponse> replies = repliesMap.getOrDefault(response.getId(), List.of()).stream()
                        .map(reply -> buildCommentResponse(reply, userId))
                        .collect(Collectors.toList());
                response.setReplies(replies);
                response.setReplyCount(replies.size());
            });
        }

        return PageResponse.<CommentResponse>builder()
                .content(responses)
                .page(page)
                .size(size)
                .totalElements(commentsPage.getTotalElements())
                .totalPages(commentsPage.getTotalPages())
                .hasNext(commentsPage.hasNext())
                .hasPrevious(commentsPage.hasPrevious())
                .build();
    }

    @Transactional
    public CommentResponse updateComment(String commentId, UpdateCommentRequest request) {
        String userId = currentUserProvider.getCurrentUserId();
        Comment comment = commentRepository.findByIdAndUserId(commentId, userId)
                .orElseThrow(() -> new AppException(ErrorCode.COMMENT_NOT_FOUND));

        comment.setContent(request.getContent());
        return buildCommentResponse(commentRepository.save(comment), userId);
    }

    @Transactional
    public void deleteComment(String commentId) {
        String userId = currentUserProvider.getCurrentUserId();
        Comment comment = commentRepository.findByIdAndUserId(commentId, userId)
                .orElseThrow(() -> new AppException(ErrorCode.COMMENT_NOT_FOUND));

        List<Comment> replies = commentRepository.findByParentCommentIdOrderByCreatedAtAsc(commentId);
        List<String> replyIds = replies.stream().map(Comment::getId).toList();
        if (!replyIds.isEmpty()) {
            likeRepository.deleteByCommentIdIn(replyIds);
        }
        commentRepository.deleteAll(replies);
        likeRepository.deleteByCommentId(commentId);
        commentRepository.delete(comment);
    }

    public long getCommentCountByPost(String postId) {
        return commentRepository.countByPostId(postId);
    }

    public CommentResponse getCommentById(String commentId) {
        String userId = currentUserProvider.getCurrentUserId();
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new AppException(ErrorCode.COMMENT_NOT_FOUND));
        return buildCommentResponse(comment, userId);
    }

    public PageResponse<CommentResponse> getRepliesByCommentId(String commentId, int page, int size) {
        String userId = currentUserProvider.getCurrentUserId();
        commentRepository.findById(commentId)
                .orElseThrow(() -> new AppException(ErrorCode.COMMENT_NOT_FOUND));

        List<Comment> allReplies = commentRepository.findByParentCommentIdOrderByCreatedAtAsc(commentId);
        int start = (page - 1) * size;
        int end = Math.min(start + size, allReplies.size());
        List<Comment> paginatedReplies = start < allReplies.size() ? allReplies.subList(start, end) : List.of();
        List<CommentResponse> responses = paginatedReplies.stream()
                .map(reply -> buildCommentResponse(reply, userId))
                .collect(Collectors.toList());
        int totalPages = (int) Math.ceil((double) allReplies.size() / size);

        return PageResponse.<CommentResponse>builder()
                .content(responses)
                .page(page)
                .size(size)
                .totalElements(allReplies.size())
                .totalPages(totalPages)
                .hasNext(end < allReplies.size())
                .hasPrevious(page > 1)
                .build();
    }

    private void validatePostExists(String postId) {
        if (!postQueryPort.exists(postId)) {
            throw new AppException(ErrorCode.POST_NOT_FOUND);
        }
    }

    private CommentResponse buildCommentResponse(Comment comment, String currentUserId) {
        CommentResponse response = commentMapper.toCommentResponse(comment);
        ProfileResponse profile = getProfile(comment.getUserId());
        if (profile != null) {
            response.setUsername(displayNameFormatter.displayName(profile));
            response.setUserAvatar(profile.getAvatar());
        }
        response.setReplies(new ArrayList<>());
        response.setReplyCount(0);
        response.setLikeCount((int) likeRepository.countByCommentId(comment.getId()));
        response.setIsLiked(likeRepository.findByUserIdAndCommentIdAndPostIdIsNull(currentUserId, comment.getId()).isPresent());
        return response;
    }

    private ProfileResponse getProfile(String userId) {
        try {
            return profileQueryPort.getProfileByUserId(userId);
        } catch (Exception exception) {
            log.warn("Failed to get profile for comment user {}: {}", userId, exception.getMessage());
            return null;
        }
    }
}
