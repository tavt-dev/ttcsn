package com.friendify.app.post.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import com.friendify.app.file.dto.UploadResponse;
import com.friendify.app.file.port.FileUploadPort;
import com.friendify.app.group.port.GroupAccessPort;
import com.friendify.app.interaction.port.InteractionCleanupPort;
import com.friendify.app.interaction.port.InteractionQueryPort;
import com.friendify.app.interaction.port.PostQueryPort;
import com.friendify.app.post.dto.response.PostResponse;
import com.friendify.app.post.entity.Post;
import com.friendify.app.post.enums.PrivacyType;
import com.friendify.app.post.mapper.PostMapper;
import com.friendify.app.post.repository.PostRepository;
import com.friendify.app.post.repository.SavedPostRepository;
import com.friendify.app.post.repository.SharedPostRepository;
import com.friendify.app.profile.dto.response.ProfileResponse;
import com.friendify.app.profile.port.ProfileQueryPort;
import com.friendify.app.shared.media.ImageType;
import com.friendify.app.shared.security.CurrentUserProvider;
import com.friendify.app.social.port.SocialGraphQueryPort;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InOrder;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.mock.web.MockMultipartFile;

@ExtendWith(MockitoExtension.class)
class PostServiceTests {

    @Mock
    DateTimeFormatter dateTimeFormatter;

    @Mock
    PostRepository postRepository;

    @Mock
    PostMapper postMapper;

    @Mock
    ProfileQueryPort profileQueryPort;

    @Mock
    SocialGraphQueryPort socialGraphQueryPort;

    @Mock
    InteractionQueryPort interactionQueryPort;

    @Mock
    InteractionCleanupPort interactionCleanupPort;

    @Mock
    GroupAccessPort groupAccessPort;

    @Mock
    FileUploadPort fileUploadPort;

    @Mock
    SavedPostRepository savedPostRepository;

    @Mock
    SharedPostRepository sharedPostRepository;

    @Mock
    CurrentUserProvider currentUserProvider;

    @InjectMocks
    PostService postService;

    @Test
    void implementsInteractionPostQueryPort() {
        assertThat(postService).isInstanceOf(PostQueryPort.class);
    }

    @Test
    void createPostUploadsImagesThroughFileUploadPort() throws Exception {
        MockMultipartFile file = new MockMultipartFile("images", "post.png", "image/png", "image".getBytes());
        Post savedBeforeUpload = Post.builder()
                .id("post-1")
                .userId("user-1")
                .content("hello")
                .privacy(PrivacyType.PUBLIC)
                .createdDate(Instant.now())
                .build();
        Post savedAfterUpload = Post.builder()
                .id("post-1")
                .userId("user-1")
                .content("hello")
                .privacy(PrivacyType.PUBLIC)
                .imageUrls(List.of("https://cdn.example/post.png"))
                .createdDate(savedBeforeUpload.getCreatedDate())
                .build();
        PostResponse mappedResponse = PostResponse.builder()
                .id("post-1")
                .userId("user-1")
                .build();
        ProfileResponse profile = ProfileResponse.builder()
                .userId("user-1")
                .username("alice")
                .build();

        when(currentUserProvider.getCurrentUserId()).thenReturn("user-1");
        when(postRepository.save(any(Post.class))).thenReturn(savedBeforeUpload, savedAfterUpload);
        when(fileUploadPort.uploadImages(eq(List.of(file)), eq(ImageType.POST_IMAGE), eq("user-1"), eq("post-1")))
                .thenReturn(List.of(new UploadResponse("public-id", 1L, 100, 100, "https://cdn.example/post.png", null)));
        when(postMapper.toPostResponse(savedAfterUpload)).thenReturn(mappedResponse);
        when(profileQueryPort.getProfileByUserId("user-1")).thenReturn(profile);
        when(dateTimeFormatter.format(savedAfterUpload.getCreatedDate())).thenReturn("now");

        PostResponse result = postService.createPost("hello", List.of(file), PrivacyType.PUBLIC, null);

        assertThat(result).isSameAs(mappedResponse);
        assertThat(savedAfterUpload.getImageUrls()).containsExactly("https://cdn.example/post.png");
        verify(fileUploadPort).uploadImages(eq(List.of(file)), eq(ImageType.POST_IMAGE), eq("user-1"), eq("post-1"));
    }

    @Test
    void deletePostCallsInteractionCleanupBeforeDeletingPost() {
        Post post = Post.builder()
                .id("post-1")
                .userId("user-1")
                .privacy(PrivacyType.PUBLIC)
                .build();

        when(currentUserProvider.getCurrentUserId()).thenReturn("user-1");
        when(postRepository.findById("post-1")).thenReturn(Optional.of(post));

        postService.deletePost("post-1");

        InOrder inOrder = inOrder(interactionCleanupPort, savedPostRepository, postRepository);
        inOrder.verify(interactionCleanupPort).deleteByPostId("post-1");
        inOrder.verify(savedPostRepository).deleteAllByPostId("post-1");
        inOrder.verify(postRepository).delete(post);
    }

    @Test
    void feedUsesSocialGraphPortForVisibility() {
        Post post = Post.builder()
                .id("post-1")
                .userId("friend-1")
                .privacy(PrivacyType.PUBLIC)
                .createdDate(Instant.now())
                .build();
        PostResponse mappedResponse = PostResponse.builder()
                .id("post-1")
                .userId("friend-1")
                .build();

        when(currentUserProvider.getCurrentUserId()).thenReturn("user-1");
        when(socialGraphQueryPort.getFriendIds("user-1")).thenReturn(List.of("friend-1"));
        when(socialGraphQueryPort.getFollowingIds("user-1")).thenReturn(List.of());
        when(socialGraphQueryPort.getBlockedUserIds("user-1")).thenReturn(List.of());
        when(postRepository.findByUserIdInWithPrivacyFilter(
                        org.mockito.ArgumentMatchers.<List<String>>any(),
                        eq("user-1"),
                        any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(post)));
        when(postMapper.toPostResponse(post)).thenReturn(mappedResponse);
        when(profileQueryPort.getProfileByUserId("friend-1")).thenReturn(ProfileResponse.builder()
                .userId("friend-1")
                .username("friend")
                .build());
        when(dateTimeFormatter.format(post.getCreatedDate())).thenReturn("now");

        var result = postService.getFeed(1, 10);

        assertThat(result.getContent()).containsExactly(mappedResponse);
        verify(socialGraphQueryPort).getFriendIds("user-1");
        verify(socialGraphQueryPort).getFollowingIds("user-1");
        verify(socialGraphQueryPort).getBlockedUserIds("user-1");
    }
}
