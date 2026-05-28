package com.friendify.app.interaction.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;

import com.friendify.app.interaction.dto.request.CreateLikeRequest;
import com.friendify.app.interaction.dto.response.LikeResponse;
import com.friendify.app.interaction.entity.Comment;
import com.friendify.app.interaction.entity.Like;
import com.friendify.app.interaction.mapper.LikeMapper;
import com.friendify.app.interaction.port.PostQueryPort;
import com.friendify.app.interaction.repository.CommentRepository;
import com.friendify.app.interaction.repository.LikeRepository;
import com.friendify.app.profile.dto.response.ProfileResponse;
import com.friendify.app.profile.port.ProfileQueryPort;
import com.friendify.app.shared.security.CurrentUserProvider;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class LikeServiceTests {

    @Mock
    LikeRepository likeRepository;

    @Mock
    CommentRepository commentRepository;

    @Mock
    PostQueryPort postQueryPort;

    @Mock
    ProfileQueryPort profileQueryPort;

    @Mock
    LikeMapper likeMapper;

    @Mock
    CurrentUserProvider currentUserProvider;

    @Mock
    ProfileDisplayNameFormatter displayNameFormatter;

    @InjectMocks
    LikeService likeService;

    @Test
    void createCommentLikeUsesProfileQueryPortForResponseEnrichment() {
        CreateLikeRequest request = CreateLikeRequest.builder()
                .commentId("comment-1")
                .build();
        Like savedLike = Like.builder()
                .id("like-1")
                .userId("user-1")
                .commentId("comment-1")
                .build();
        LikeResponse response = LikeResponse.builder()
                .id("like-1")
                .userId("user-1")
                .commentId("comment-1")
                .build();
        ProfileResponse profile = ProfileResponse.builder()
                .userId("user-1")
                .firstName("Alice")
                .lastName("Nguyen")
                .avatar("avatar.png")
                .build();

        when(currentUserProvider.getCurrentUserId()).thenReturn("user-1");
        when(commentRepository.findById("comment-1")).thenReturn(Optional.of(Comment.builder().id("comment-1").build()));
        when(likeRepository.findByUserIdAndCommentIdAndPostIdIsNull("user-1", "comment-1")).thenReturn(Optional.empty());
        when(likeRepository.save(org.mockito.ArgumentMatchers.any(Like.class))).thenReturn(savedLike);
        when(likeMapper.toLikeResponse(savedLike)).thenReturn(response);
        when(profileQueryPort.getProfileByUserId("user-1")).thenReturn(profile);
        when(displayNameFormatter.displayName(profile)).thenReturn("Alice Nguyen");

        LikeResponse result = likeService.createLike(request);

        assertThat(result.getUsername()).isEqualTo("Alice Nguyen");
        assertThat(result.getUserAvatar()).isEqualTo("avatar.png");
        verify(profileQueryPort).getProfileByUserId("user-1");
    }
}
