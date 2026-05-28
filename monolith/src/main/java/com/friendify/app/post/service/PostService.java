package com.friendify.app.post.service;

import java.io.IOException;
import java.time.Instant;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import com.friendify.app.file.port.FileUploadPort;
import com.friendify.app.group.port.GroupAccessPort;
import com.friendify.app.interaction.port.InteractionCleanupPort;
import com.friendify.app.interaction.port.InteractionQueryPort;
import com.friendify.app.post.dto.response.PostResponse;
import com.friendify.app.post.entity.Post;
import com.friendify.app.post.entity.SavedPost;
import com.friendify.app.post.entity.SharedPost;
import com.friendify.app.post.enums.PrivacyType;
import com.friendify.app.post.mapper.PostMapper;
import com.friendify.app.post.repository.PostRepository;
import com.friendify.app.post.repository.SavedPostRepository;
import com.friendify.app.post.repository.SharedPostRepository;
import com.friendify.app.profile.dto.response.ProfileResponse;
import com.friendify.app.profile.port.ProfileQueryPort;
import com.friendify.app.shared.dto.PageResponse;
import com.friendify.app.shared.exception.AppException;
import com.friendify.app.shared.exception.ErrorCode;
import com.friendify.app.shared.media.ImageType;
import com.friendify.app.shared.security.CurrentUserProvider;
import com.friendify.app.social.port.SocialGraphQueryPort;
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
import org.springframework.web.multipart.MultipartFile;

@Slf4j
@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class PostService implements com.friendify.app.interaction.port.PostQueryPort {
    DateTimeFormatter dateTimeFormatter;
    PostRepository postRepository;
    PostMapper postMapper;
    ProfileQueryPort profileQueryPort;
    SocialGraphQueryPort socialGraphQueryPort;
    InteractionQueryPort interactionQueryPort;
    InteractionCleanupPort interactionCleanupPort;
    GroupAccessPort groupAccessPort;
    FileUploadPort fileUploadPort;
    SavedPostRepository savedPostRepository;
    SharedPostRepository sharedPostRepository;
    CurrentUserProvider currentUserProvider;

    public PostResponse createPost(String content, List<MultipartFile> images, PrivacyType privacy, String groupId) {
        return createPostInternal(content, images, null, privacy, groupId);
    }

    public PostResponse createPostWithUrls(String content, List<String> imageUrls, PrivacyType privacy, String groupId) {
        return createPostInternal(content, null, imageUrls, privacy, groupId);
    }

    private PostResponse createPostInternal(
            String content, List<MultipartFile> images, List<String> imageUrls, PrivacyType privacy, String groupId) {
        String userId = currentUserProvider.getCurrentUserId();
        boolean hasContent = content != null && !content.trim().isEmpty();
        boolean hasImages = images != null && !images.isEmpty();
        boolean hasImageUrls = imageUrls != null && !imageUrls.isEmpty();
        if (!hasContent && !hasImages && !hasImageUrls) {
            throw new AppException(ErrorCode.POST_EMPTY);
        }

        String normalizedGroupId = hasText(groupId) ? groupId : null;
        if (normalizedGroupId != null && !groupAccessPort.canPost(normalizedGroupId, userId)) {
            throw new AppException(ErrorCode.UNAUTHORIZED);
        }

        Post post = Post.builder()
                .content(hasContent ? content : null)
                .userId(userId)
                .privacy(privacy == null ? PrivacyType.PUBLIC : privacy)
                .groupId(normalizedGroupId)
                .originalPostId(null)
                .createdDate(Instant.now())
                .modifiedDate(Instant.now())
                .build();
        post = postRepository.save(post);

        if (hasImages) {
            post.setImageUrls(uploadPostImages(images, userId, post.getId()));
            post.setModifiedDate(Instant.now());
            post = postRepository.save(post);
        } else if (hasImageUrls) {
            post.setImageUrls(imageUrls);
            post.setModifiedDate(Instant.now());
            post = postRepository.save(post);
        }

        return buildPostResponse(post, userId);
    }

    public PageResponse<PostResponse> getMyPosts(int page, int size) {
        String userId = currentUserProvider.getCurrentUserId();
        ProfileResponse profile = getUserProfile(userId);
        var pageData = postRepository.findAllByUserId(userId, pageRequest(page, size, "createdDate"));
        var posts = pageData.getContent().stream()
                .map(post -> enrich(postMapper.toPostResponse(post), post, profile))
                .toList();
        return toPageResponse(pageData, page, posts);
    }

    public void savePost(String postId) {
        String userId = currentUserProvider.getCurrentUserId();
        postRepository.findById(postId).orElseThrow(() -> new AppException(ErrorCode.POST_NOT_FOUND));
        if (savedPostRepository.existsByUserIdAndPostId(userId, postId)) {
            throw new AppException(ErrorCode.POST_ALREADY_SAVED);
        }
        savedPostRepository.save(SavedPost.builder()
                .userId(userId)
                .postId(postId)
                .savedDate(Instant.now())
                .build());
    }

    public void unsavePost(String postId) {
        String userId = currentUserProvider.getCurrentUserId();
        SavedPost savedPost = savedPostRepository.findByUserIdAndPostId(userId, postId)
                .orElseThrow(() -> new AppException(ErrorCode.POST_NOT_SAVED));
        savedPostRepository.delete(savedPost);
    }

    public PageResponse<PostResponse> getSavedPosts(int page, int size) {
        String userId = currentUserProvider.getCurrentUserId();
        var pageData = savedPostRepository.findAllByUserId(userId, pageRequest(page, size, "savedDate"));
        List<String> postIds = pageData.getContent().stream().map(SavedPost::getPostId).toList();
        if (postIds.isEmpty()) {
            return toPageResponse(pageData, page, List.of());
        }
        var postMap = postRepository.findAllById(postIds).stream().collect(Collectors.toMap(Post::getId, post -> post));
        var posts = pageData.getContent().stream()
                .map(savedPost -> postMap.get(savedPost.getPostId()))
                .filter(post -> post != null)
                .map(post -> buildPostResponse(post, post.getUserId()))
                .toList();
        return toPageResponse(pageData, page, posts);
    }

    public PostResponse sharePost(String postId, String content) {
        String userId = currentUserProvider.getCurrentUserId();
        Post originalPost = postRepository.findById(postId).orElseThrow(() -> new AppException(ErrorCode.POST_NOT_FOUND));
        if (originalPost.getPrivacy() == PrivacyType.PRIVATE && !originalPost.getUserId().equals(userId)) {
            throw new AppException(ErrorCode.UNAUTHORIZED);
        }

        sharedPostRepository.save(SharedPost.builder()
                .userId(userId)
                .postId(postId)
                .originalPostUserId(originalPost.getUserId())
                .content(content)
                .sharedDate(Instant.now())
                .build());

        Post newPost = postRepository.save(Post.builder()
                .userId(userId)
                .content(hasText(content) ? content : null)
                .imageUrls(originalPost.getImageUrls())
                .privacy(PrivacyType.PUBLIC)
                .originalPostId(postId)
                .createdDate(Instant.now())
                .modifiedDate(Instant.now())
                .build());
        return buildPostResponse(newPost, userId);
    }

    public PageResponse<PostResponse> getSharedPosts(String postId, int page, int size) {
        var pageData = sharedPostRepository.findAllByPostId(postId, pageRequest(page, size, "sharedDate"));
        Post originalPost = postRepository.findById(postId).orElse(null);
        if (originalPost == null) {
            return toPageResponse(pageData, page, List.of());
        }
        var posts = pageData.getContent().stream()
                .map(sharedPost -> buildPostResponse(originalPost, sharedPost.getUserId()))
                .toList();
        return toPageResponse(pageData, page, posts);
    }

    public long getShareCount(String postId) {
        return sharedPostRepository.countByPostId(postId);
    }

    public boolean isPostSaved(String postId) {
        return savedPostRepository.existsByUserIdAndPostId(currentUserProvider.getCurrentUserId(), postId);
    }

    public PostResponse getPostById(String postId) {
        String userId = currentUserProvider.getCurrentUserId();
        Post post = postRepository.findById(postId).orElseThrow(() -> new AppException(ErrorCode.POST_NOT_FOUND));
        if (post.getPrivacy() == PrivacyType.PRIVATE && !post.getUserId().equals(userId)) {
            throw new AppException(ErrorCode.UNAUTHORIZED);
        }
        if (hasText(post.getGroupId()) && !groupAccessPort.canView(post.getGroupId(), userId)) {
            throw new AppException(ErrorCode.UNAUTHORIZED);
        }
        return buildPostResponse(post, post.getUserId());
    }

    public PostResponse updatePost(String postId, String content, List<MultipartFile> images, PrivacyType privacy) {
        return updatePostInternal(postId, content, images, null, privacy);
    }

    public PostResponse updatePostWithUrls(String postId, String content, List<String> imageUrls, PrivacyType privacy) {
        return updatePostInternal(postId, content, null, imageUrls, privacy);
    }

    private PostResponse updatePostInternal(
            String postId, String content, List<MultipartFile> images, List<String> imageUrls, PrivacyType privacy) {
        String userId = currentUserProvider.getCurrentUserId();
        Post post = postRepository.findById(postId).orElseThrow(() -> new AppException(ErrorCode.POST_NOT_FOUND));
        if (!post.getUserId().equals(userId)) {
            throw new AppException(ErrorCode.POST_NOT_OWNER);
        }

        boolean hasContent = content != null && !content.trim().isEmpty();
        boolean hasImages = images != null && !images.isEmpty();
        boolean hasImageUrls = imageUrls != null && !imageUrls.isEmpty();
        if (!hasContent && !hasImages && !hasImageUrls && (post.getImageUrls() == null || post.getImageUrls().isEmpty())) {
            throw new AppException(ErrorCode.POST_EMPTY);
        }

        if (content != null) {
            post.setContent(content);
        }
        if (privacy != null) {
            post.setPrivacy(privacy);
        }
        if (hasImages) {
            post.setImageUrls(uploadPostImages(images, userId, post.getId()));
        } else if (hasImageUrls) {
            post.setImageUrls(imageUrls);
        }
        post.setModifiedDate(Instant.now());
        return buildPostResponse(postRepository.save(post), userId);
    }

    @Transactional
    public void deletePost(String postId) {
        String userId = currentUserProvider.getCurrentUserId();
        Post post = postRepository.findById(postId).orElseThrow(() -> new AppException(ErrorCode.POST_NOT_FOUND));
        if (!post.getUserId().equals(userId)) {
            throw new AppException(ErrorCode.POST_NOT_OWNER);
        }

        // Fail-fast cleanup: if interaction cleanup fails, the post delete does not continue.
        interactionCleanupPort.deleteByPostId(postId);
        savedPostRepository.deleteAllByPostId(postId);
        if (post.getOriginalPostId() == null) {
            sharedPostRepository.deleteAllByPostId(postId);
            postRepository.deleteAllByOriginalPostId(postId);
        } else {
            sharedPostRepository.findByUserIdAndPostId(userId, post.getOriginalPostId())
                    .ifPresent(sharedPostRepository::delete);
        }
        postRepository.delete(post);
    }

    public PageResponse<PostResponse> getPostsByUserId(String userId, int page, int size) {
        var pageData = postRepository.findByUserIdWithPrivacy(userId, pageRequest(page, size, "createdDate"));
        ProfileResponse profile = getUserProfile(userId);
        var posts = pageData.getContent().stream()
                .filter(post -> !hasText(post.getGroupId()))
                .map(post -> enrich(postMapper.toPostResponse(post), post, profile))
                .toList();
        return toPageResponse(pageData, page, posts);
    }

    public PageResponse<PostResponse> getMySharedPosts(int page, int size) {
        String userId = currentUserProvider.getCurrentUserId();
        ProfileResponse profile = getUserProfile(userId);
        var pageData = postRepository.findByUserIdAndOriginalPostIdIsNotNull(userId, pageRequest(page, size, "createdDate"));
        var posts = pageData.getContent().stream()
                .map(post -> enrich(postMapper.toPostResponse(post), post, profile))
                .toList();
        return toPageResponse(pageData, page, posts);
    }

    public long getSavedCount() {
        return savedPostRepository.findAllByUserId(currentUserProvider.getCurrentUserId(), Pageable.unpaged()).getTotalElements();
    }

    public PageResponse<PostResponse> searchPosts(String keyword, int page, int size) {
        if (!hasText(keyword)) {
            throw new AppException(ErrorCode.INVALID_KEY);
        }
        String userId = currentUserProvider.getCurrentUserId();
        List<String> blockedUserIds = nonEmptyIds(getBlockedUserIds(userId));
        var pageData = postRepository.searchPublicPosts(userId, keyword.trim(), blockedUserIds, pageRequest(page, size, "createdDate"));
        var posts = pageData.getContent().stream()
                .filter(post -> !hasText(post.getGroupId()))
                .map(post -> buildPostResponse(post, post.getUserId()))
                .toList();
        return toPageResponse(pageData, page, posts);
    }

    public PageResponse<PostResponse> getPublicPosts(int page, int size) {
        String userId = currentUserProvider.getCurrentUserId();
        List<String> blockedUserIds = nonEmptyIds(getBlockedUserIds(userId));
        var pageData = postRepository.findByPrivacyAndUserIdNotIn(PrivacyType.PUBLIC, blockedUserIds, pageRequest(page, size, "createdDate"));
        var posts = pageData.getContent().stream()
                .filter(post -> !hasText(post.getGroupId()))
                .map(post -> buildPostResponse(post, post.getUserId()))
                .toList();
        return toPageResponse(pageData, page, posts);
    }

    public PageResponse<PostResponse> getFeed(int page, int size) {
        String userId = currentUserProvider.getCurrentUserId();
        Set<String> allowedUserIds = new HashSet<>(socialGraphQueryPort.getFriendIds(userId));
        allowedUserIds.addAll(socialGraphQueryPort.getFollowingIds(userId));
        allowedUserIds.add(userId);
        allowedUserIds.removeAll(getBlockedUserIds(userId));
        if (allowedUserIds.isEmpty()) {
            return PageResponse.<PostResponse>builder().content(List.of()).page(page).size(size).build();
        }
        var pageData = postRepository.findByUserIdInWithPrivacyFilter(
                List.copyOf(allowedUserIds), userId, pageRequest(page, size, "createdDate"));
        var posts = pageData.getContent().stream()
                .filter(post -> !hasText(post.getGroupId()))
                .map(post -> buildPostResponse(post, post.getUserId()))
                .toList();
        return toPageResponse(pageData, page, posts);
    }

    public PageResponse<PostResponse> getPostsByGroup(String groupId, int page, int size) {
        String userId = currentUserProvider.getCurrentUserId();
        if (!groupAccessPort.canView(groupId, userId)) {
            throw new AppException(ErrorCode.UNAUTHORIZED);
        }
        var pageData = postRepository.findByGroupIdWithPrivacy(groupId, userId, pageRequest(page, size, "createdDate"));
        var posts = pageData.getContent().stream()
                .map(post -> buildPostResponse(post, post.getUserId()))
                .toList();
        return toPageResponse(pageData, page, posts);
    }

    public boolean checkPostExists(String postId) {
        return postRepository.existsById(postId);
    }

    @Override
    public boolean exists(String postId) {
        return checkPostExists(postId);
    }

    private List<String> uploadPostImages(List<MultipartFile> images, String userId, String postId) {
        try {
            return fileUploadPort.uploadImages(images, ImageType.POST_IMAGE, userId, postId).stream()
                    .map(uploadResponse -> uploadResponse.getSecureUrl())
                    .toList();
        } catch (IOException exception) {
            log.error("Failed to upload images for post {}", postId, exception);
            throw new AppException(ErrorCode.POST_IMAGE_UPLOAD_FAILED);
        }
    }

    private Set<String> getBlockedUserIds(String userId) {
        return new HashSet<>(socialGraphQueryPort.getBlockedUserIds(userId));
    }

    private List<String> nonEmptyIds(Set<String> ids) {
        if (ids == null || ids.isEmpty()) {
            return List.of("__friendify_no_matching_user__");
        }
        return List.copyOf(ids);
    }

    private ProfileResponse getUserProfile(String userId) {
        try {
            return profileQueryPort.getProfileByUserId(userId);
        } catch (Exception exception) {
            log.warn("Failed to get profile for post user {}: {}", userId, exception.getMessage());
            return null;
        }
    }

    private PostResponse buildPostResponse(Post post, String userId) {
        return enrich(postMapper.toPostResponse(post), post, getUserProfile(userId));
    }

    private PostResponse enrich(PostResponse response, Post post, ProfileResponse profile) {
        response.setCreated(dateTimeFormatter.format(post.getCreatedDate()));
        if (profile != null) {
            response.setUsername(displayName(profile));
            response.setUserAvatar(profile.getAvatar());
        }
        if (hasText(post.getGroupId())) {
            response.setGroupId(post.getGroupId());
            try {
                response.setGroupName(groupAccessPort.getGroup(post.getGroupId()).getName());
            } catch (Exception exception) {
                log.warn("Failed to get group info for post {}: {}", post.getId(), exception.getMessage());
            }
        }
        if (hasText(post.getOriginalPostId())) {
            postRepository.findById(post.getOriginalPostId()).ifPresent(originalPost -> {
                response.setOriginalPostUserId(originalPost.getUserId());
                ProfileResponse originalProfile = getUserProfile(originalPost.getUserId());
                if (originalProfile != null) {
                    response.setOriginalPostUsername(displayName(originalProfile));
                    response.setOriginalPostUserAvatar(originalProfile.getAvatar());
                }
            });
        }
        String currentUserId = currentUserProvider.getCurrentUserId();
        response.setIsSaved(savedPostRepository.existsByUserIdAndPostId(currentUserId, post.getId()));
        response.setIsOwnerPost(post.getUserId().equals(currentUserId));
        response.setShareCount(sharedPostRepository.countByPostId(post.getId()));
        response.setLikeCount((int) interactionQueryPort.countLikesByPostId(post.getId()));
        response.setCommentCount((int) interactionQueryPort.countCommentsByPostId(post.getId()));
        response.setIsLiked(interactionQueryPort.isLikedByCurrentUser(post.getId(), currentUserId));
        return response;
    }

    private String displayName(ProfileResponse profile) {
        if (hasText(profile.getFirstName()) && hasText(profile.getLastName())) {
            return (profile.getFirstName().trim() + " " + profile.getLastName().trim()).trim();
        }
        if (hasText(profile.getLastName())) {
            return profile.getLastName().trim();
        }
        if (hasText(profile.getFirstName())) {
            return profile.getFirstName().trim();
        }
        return profile.getUsername() != null ? profile.getUsername() : "";
    }

    private Pageable pageRequest(int page, int size, String sortField) {
        return PageRequest.of(page - 1, size, Sort.by(sortField).descending());
    }

    private <T> PageResponse<T> toPageResponse(Page<?> pageData, int page, List<T> content) {
        return PageResponse.<T>builder()
                .content(content)
                .page(page)
                .size(pageData.getSize())
                .totalElements(pageData.getTotalElements())
                .totalPages(pageData.getTotalPages())
                .hasNext(pageData.hasNext())
                .hasPrevious(pageData.hasPrevious())
                .build();
    }

    private boolean hasText(String value) {
        return value != null && !value.trim().isEmpty();
    }
}
