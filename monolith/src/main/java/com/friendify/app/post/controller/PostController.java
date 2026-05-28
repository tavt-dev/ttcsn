package com.friendify.app.post.controller;

import java.util.List;

import com.friendify.app.post.dto.request.PostRequest;
import com.friendify.app.post.dto.request.UpdatePostRequest;
import com.friendify.app.post.dto.response.PostResponse;
import com.friendify.app.post.enums.PrivacyType;
import com.friendify.app.post.service.PostService;
import com.friendify.app.shared.dto.ApiResponse;
import com.friendify.app.shared.dto.PageResponse;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/v1/post")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class PostController {
    PostService postService;

    @PostMapping(value = "/create", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    ApiResponse<PostResponse> createPost(
            @RequestParam(value = "content", required = false) String content,
            @RequestPart(value = "images", required = false) List<MultipartFile> images,
            @RequestParam(value = "privacy", required = false) PrivacyType privacy,
            @RequestParam(value = "groupId", required = false) String groupId) {
        return ApiResponse.<PostResponse>builder()
                .result(postService.createPost(content, images, privacy, groupId))
                .build();
    }

    @PostMapping(value = "/json", consumes = MediaType.APPLICATION_JSON_VALUE)
    ApiResponse<PostResponse> createPostWithJson(@RequestBody PostRequest request) {
        return ApiResponse.<PostResponse>builder()
                .result(postService.createPostWithUrls(
                        request.getContent(), request.getImageUrls(), request.getPrivacy(), request.getGroupId()))
                .build();
    }

    @GetMapping("/my-posts")
    ApiResponse<PageResponse<PostResponse>> myPosts(
            @RequestParam(value = "page", required = false, defaultValue = "1") int page,
            @RequestParam(value = "size", required = false, defaultValue = "10") int size) {
        return ApiResponse.<PageResponse<PostResponse>>builder()
                .result(postService.getMyPosts(page, size))
                .build();
    }

    @PostMapping("/save/{postId}")
    ApiResponse<Void> savePost(@PathVariable String postId) {
        postService.savePost(postId);
        return ApiResponse.<Void>builder().build();
    }

    @DeleteMapping("/unsave/{postId}")
    ApiResponse<Void> unsavePost(@PathVariable String postId) {
        postService.unsavePost(postId);
        return ApiResponse.<Void>builder().build();
    }

    @GetMapping("/saved-posts")
    ApiResponse<PageResponse<PostResponse>> getSavedPosts(
            @RequestParam(value = "page", required = false, defaultValue = "1") int page,
            @RequestParam(value = "size", required = false, defaultValue = "10") int size) {
        return ApiResponse.<PageResponse<PostResponse>>builder()
                .result(postService.getSavedPosts(page, size))
                .build();
    }

    @PostMapping("/share/{postId}")
    ApiResponse<PostResponse> sharePost(
            @PathVariable String postId,
            @RequestParam(value = "content", required = false) String content) {
        return ApiResponse.<PostResponse>builder()
                .result(postService.sharePost(postId, content))
                .build();
    }

    @GetMapping("/shared-posts/{postId}")
    ApiResponse<PageResponse<PostResponse>> getSharedPosts(
            @PathVariable String postId,
            @RequestParam(value = "page", required = false, defaultValue = "1") int page,
            @RequestParam(value = "size", required = false, defaultValue = "10") int size) {
        return ApiResponse.<PageResponse<PostResponse>>builder()
                .result(postService.getSharedPosts(postId, page, size))
                .build();
    }

    @GetMapping("/share-count/{postId}")
    ApiResponse<Long> getShareCount(@PathVariable String postId) {
        return ApiResponse.<Long>builder()
                .result(postService.getShareCount(postId))
                .build();
    }

    @GetMapping("/is-saved/{postId}")
    ApiResponse<Boolean> isPostSaved(@PathVariable String postId) {
        return ApiResponse.<Boolean>builder()
                .result(postService.isPostSaved(postId))
                .build();
    }

    @GetMapping("/user/{userId}")
    ApiResponse<PageResponse<PostResponse>> getPostsByUser(
            @PathVariable String userId,
            @RequestParam(value = "page", required = false, defaultValue = "1") int page,
            @RequestParam(value = "size", required = false, defaultValue = "10") int size) {
        return ApiResponse.<PageResponse<PostResponse>>builder()
                .result(postService.getPostsByUserId(userId, page, size))
                .build();
    }

    @GetMapping("/my-shared-posts")
    ApiResponse<PageResponse<PostResponse>> getMySharedPosts(
            @RequestParam(value = "page", required = false, defaultValue = "1") int page,
            @RequestParam(value = "size", required = false, defaultValue = "10") int size) {
        return ApiResponse.<PageResponse<PostResponse>>builder()
                .result(postService.getMySharedPosts(page, size))
                .build();
    }

    @GetMapping("/saved-count")
    ApiResponse<Long> getSavedCount() {
        return ApiResponse.<Long>builder()
                .result(postService.getSavedCount())
                .build();
    }

    @GetMapping("/search")
    ApiResponse<PageResponse<PostResponse>> searchPosts(
            @RequestParam("keyword") String keyword,
            @RequestParam(value = "page", required = false, defaultValue = "1") int page,
            @RequestParam(value = "size", required = false, defaultValue = "10") int size) {
        return ApiResponse.<PageResponse<PostResponse>>builder()
                .result(postService.searchPosts(keyword, page, size))
                .build();
    }

    @GetMapping("/{postId}")
    ApiResponse<PostResponse> getPostById(@PathVariable String postId) {
        return ApiResponse.<PostResponse>builder()
                .result(postService.getPostById(postId))
                .build();
    }

    @PutMapping(value = "/{postId}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    ApiResponse<PostResponse> updatePost(
            @PathVariable String postId,
            @RequestParam(value = "content", required = false) String content,
            @RequestPart(value = "images", required = false) List<MultipartFile> images,
            @RequestParam(value = "privacy", required = false) PrivacyType privacy) {
        return ApiResponse.<PostResponse>builder()
                .result(postService.updatePost(postId, content, images, privacy))
                .build();
    }

    @PutMapping(value = "/{postId}/json", consumes = MediaType.APPLICATION_JSON_VALUE)
    ApiResponse<PostResponse> updatePostWithJson(
            @PathVariable String postId,
            @RequestBody UpdatePostRequest request) {
        return ApiResponse.<PostResponse>builder()
                .result(postService.updatePostWithUrls(
                        postId, request.getContent(), request.getImageUrls(), request.getPrivacy()))
                .build();
    }

    @DeleteMapping("/{postId}")
    ApiResponse<Void> deletePost(@PathVariable String postId) {
        postService.deletePost(postId);
        return ApiResponse.<Void>builder().build();
    }

    @GetMapping("/public")
    ApiResponse<PageResponse<PostResponse>> getPublicPosts(
            @RequestParam(value = "page", required = false, defaultValue = "1") int page,
            @RequestParam(value = "size", required = false, defaultValue = "10") int size) {
        return ApiResponse.<PageResponse<PostResponse>>builder()
                .result(postService.getPublicPosts(page, size))
                .build();
    }

    @GetMapping("/feed")
    ApiResponse<PageResponse<PostResponse>> getFeed(
            @RequestParam(value = "page", required = false, defaultValue = "1") int page,
            @RequestParam(value = "size", required = false, defaultValue = "10") int size) {
        return ApiResponse.<PageResponse<PostResponse>>builder()
                .result(postService.getFeed(page, size))
                .build();
    }

    @GetMapping("/group/{groupId}")
    ApiResponse<PageResponse<PostResponse>> getPostsByGroup(
            @PathVariable String groupId,
            @RequestParam(value = "page", required = false, defaultValue = "1") int page,
            @RequestParam(value = "size", required = false, defaultValue = "10") int size) {
        return ApiResponse.<PageResponse<PostResponse>>builder()
                .result(postService.getPostsByGroup(groupId, page, size))
                .build();
    }
}
