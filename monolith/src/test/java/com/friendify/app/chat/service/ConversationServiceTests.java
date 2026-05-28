package com.friendify.app.chat.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;

import com.friendify.app.chat.dto.request.ConversationRequest;
import com.friendify.app.chat.dto.response.ConversationResponse;
import com.friendify.app.chat.entity.Conversation;
import com.friendify.app.chat.enums.TypeConversation;
import com.friendify.app.chat.mapper.ConversationMapper;
import com.friendify.app.chat.repository.ConversationRepository;
import com.friendify.app.profile.dto.response.ProfileResponse;
import com.friendify.app.profile.port.ProfileQueryPort;
import com.friendify.app.shared.security.CurrentUserProvider;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ConversationServiceTests {

    @Mock
    ConversationRepository conversationRepository;

    @Mock
    ProfileQueryPort profileQueryPort;

    @Mock
    ConversationMapper conversationMapper;

    @Mock
    CurrentUserProvider currentUserProvider;

    @InjectMocks
    ConversationService conversationService;

    @Test
    void createConversationUsesProfileQueryPort() {
        ConversationRequest request = ConversationRequest.builder()
                .participantIds(List.of("user-2"))
                .build();
        ProfileResponse currentUser = ProfileResponse.builder()
                .userId("user-1")
                .username("alice")
                .build();
        ProfileResponse otherUser = ProfileResponse.builder()
                .userId("user-2")
                .username("bob")
                .build();
        Conversation savedConversation = Conversation.builder()
                .id("conversation-1")
                .typeConversation(TypeConversation.DIRECT)
                .participantsHash("user-1_user-2")
                .participants(List.of())
                .build();
        ConversationResponse mappedResponse = ConversationResponse.builder()
                .id("conversation-1")
                .typeConversation(TypeConversation.DIRECT)
                .build();

        when(currentUserProvider.getCurrentUserId()).thenReturn("user-1");
        when(profileQueryPort.getProfileByUserId("user-1")).thenReturn(currentUser);
        when(profileQueryPort.getProfilesByUserIds(List.of("user-2"))).thenReturn(List.of(otherUser));
        when(conversationRepository.findByParticipantsHash("user-1_user-2")).thenReturn(Optional.empty());
        when(conversationRepository.save(org.mockito.ArgumentMatchers.any(Conversation.class))).thenReturn(savedConversation);
        when(conversationMapper.toConversationResponse(savedConversation)).thenReturn(mappedResponse);

        ConversationResponse result = conversationService.create(request);

        assertThat(result).isSameAs(mappedResponse);
        verify(profileQueryPort).getProfileByUserId("user-1");
        verify(profileQueryPort).getProfilesByUserIds(List.of("user-2"));
    }
}
