package com.friendify.app.interaction.service;

import java.util.List;
import java.util.stream.Collectors;

import com.friendify.app.interaction.dto.request.CreateLikeRequest;
import com.friendify.app.interaction.dto.response.LikeResponse;
import com.friendify.app.interaction.entity.Like;
import com.friendify.app.interaction.mapper.LikeMapper;
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
public class LikeService {
    LikeRepository likeRepository;
    CommentRepository commentRepository;
    PostQueryPort postQueryPort;
    ProfileQueryPort profileQueryPort;
    LikeMapper likeMapper;
    CurrentUserProvider currentUserProvider;
    ProfileDisplayNameFormatter displayNameFormatter;

    @Transactional
    public LikeResponse createLike(CreateLikeRequest request) {
        String userId = currentUserProvider.getCurrentUserId();

        if (request.getPostId() != null) {
            validatePostExists(request.getPostId());
            likeRepository.findByUserIdAndPostIdAndCommentIdIsNull(userId, request.getPostId())
                    .ifPresent(like -> {
                        throw new AppException(ErrorCode.ALREADY_LIKED);
                    });
        } else {
            if (commentRepository.findById(request.getCommentId()).isEmpty()) {
                throw new AppException(ErrorCode.COMMENT_NOT_FOUND);
            }
            likeRepository.findByUserIdAndCommentIdAndPostIdIsNull(userId, request.getCommentId())
                    .ifPresent(like -> {
                        throw new AppException(ErrorCode.ALREADY_LIKED);
                    });
        }

        Like like = likeRepository.save(Like.builder()
                .userId(userId)
                .postId(request.getPostId())
                .commentId(request.getCommentId())
                .build());

        return buildLikeResponse(like);
    }

    @Transactional
    public void unlike(String likeId) {
        String userId = currentUserProvider.getCurrentUserId();
        Like like = likeRepository.findById(likeId)
                .orElseThrow(() -> new AppException(ErrorCode.LIKE_NOT_FOUND));

        if (!like.getUserId().equals(userId)) {
            throw new AppException(ErrorCode.UNAUTHORIZED);
        }

        likeRepository.delete(like);
    }

    @Transactional
    public void unlikeByPost(String postId) {
        String userId = currentUserProvider.getCurrentUserId();
        Like like = likeRepository.findByUserIdAndPostIdAndCommentIdIsNull(userId, postId)
                .orElseThrow(() -> new AppException(ErrorCode.LIKE_NOT_FOUND));
        likeRepository.delete(like);
    }

    @Transactional
    public void unlikeByComment(String commentId) {
        String userId = currentUserProvider.getCurrentUserId();
        Like like = likeRepository.findByUserIdAndCommentIdAndPostIdIsNull(userId, commentId)
                .orElseThrow(() -> new AppException(ErrorCode.LIKE_NOT_FOUND));
        likeRepository.delete(like);
    }

    public PageResponse<LikeResponse> getLikesByPost(String postId, int page, int size) {
        Pageable pageable = PageRequest.of(page - 1, size, Sort.by("createdAt").descending());
        Page<Like> likesPage = likeRepository.findByPostIdAndCommentIdIsNull(postId, pageable);
        List<LikeResponse> responses = likesPage.getContent().stream()
                .map(this::buildLikeResponse)
                .collect(Collectors.toList());

        return PageResponse.<LikeResponse>builder()
                .content(responses)
                .page(page)
                .size(size)
                .totalElements(likesPage.getTotalElements())
                .totalPages(likesPage.getTotalPages())
                .hasNext(likesPage.hasNext())
                .hasPrevious(likesPage.hasPrevious())
                .build();
    }

    public long getLikeCountByPost(String postId) {
        return likeRepository.countByPostId(postId);
    }

    public boolean isPostLiked(String postId) {
        return isPostLiked(postId, currentUserProvider.getCurrentUserId());
    }

    public boolean isPostLiked(String postId, String userId) {
        return likeRepository.findByUserIdAndPostIdAndCommentIdIsNull(userId, postId).isPresent();
    }

    private void validatePostExists(String postId) {
        if (!postQueryPort.exists(postId)) {
            throw new AppException(ErrorCode.POST_NOT_FOUND);
        }
    }

    private LikeResponse buildLikeResponse(Like like) {
        LikeResponse response = likeMapper.toLikeResponse(like);
        ProfileResponse profile = getProfile(like.getUserId());
        if (profile != null) {
            response.setUsername(displayNameFormatter.displayName(profile));
            response.setUserAvatar(profile.getAvatar());
        }
        return response;
    }

    private ProfileResponse getProfile(String userId) {
        try {
            return profileQueryPort.getProfileByUserId(userId);
        } catch (Exception exception) {
            log.warn("Failed to get profile for like user {}: {}", userId, exception.getMessage());
            return null;
        }
    }
}
