package com.friendify.app.interaction.service;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;

import com.friendify.app.interaction.repository.CommentRepository;
import com.friendify.app.interaction.repository.LikeRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class InteractionPortServiceTests {

    @Mock
    LikeRepository likeRepository;

    @Mock
    CommentRepository commentRepository;

    @InjectMocks
    InteractionPortService interactionPortService;

    @Test
    void deleteByPostIdDeletesCommentsAndRelatedLikes() {
        when(commentRepository.findIdsByPostId("post-1")).thenReturn(List.of("comment-1", "comment-2"));

        interactionPortService.deleteByPostId("post-1");

        verify(likeRepository).deleteByCommentIdIn(List.of("comment-1", "comment-2"));
        verify(commentRepository).deleteByPostId("post-1");
        verify(likeRepository).deleteByPostId("post-1");
    }
}
