package com.friendify.app.chat.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;

import com.friendify.app.chat.dto.request.ChatMessageRequest;
import com.friendify.app.chat.dto.response.ChatMessageResponse;
import com.friendify.app.chat.entity.ChatMessage;
import com.friendify.app.chat.entity.Conversation;
import com.friendify.app.chat.entity.ParticipantInfo;
import com.friendify.app.chat.enums.ParticipantRole;
import com.friendify.app.chat.mapper.ChatMessageMapper;
import com.friendify.app.chat.repository.ChatMessageRepository;
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
class ChatMessageServiceTests {

    @Mock
    ChatMessageRepository chatMessageRepository;

    @Mock
    ChatMessageMapper chatMessageMapper;

    @Mock
    ProfileQueryPort profileQueryPort;

    @Mock
    ConversationRepository conversationRepository;

    @Mock
    CurrentUserProvider currentUserProvider;

    @InjectMocks
    ChatMessageService chatMessageService;

    @Test
    void createMessageUsesProfileQueryPort() {
        ChatMessageRequest request = ChatMessageRequest.builder()
                .conversationId("conversation-1")
                .message("hello")
                .build();
        Conversation conversation = Conversation.builder()
                .id("conversation-1")
                .participants(List.of(ParticipantInfo.builder()
                        .userId("user-1")
                        .role(ParticipantRole.ADMIN)
                        .build()))
                .build();
        ProfileResponse profile = ProfileResponse.builder()
                .userId("user-1")
                .username("alice")
                .build();
        ChatMessage mappedMessage = ChatMessage.builder()
                .conversationId("conversation-1")
                .message("hello")
                .build();
        ChatMessage savedMessage = ChatMessage.builder()
                .id("message-1")
                .conversationId("conversation-1")
                .message("hello")
                .sender(ParticipantInfo.builder()
                        .userId("user-1")
                        .username("alice")
                        .role(ParticipantRole.ADMIN)
                        .build())
                .build();
        ChatMessageResponse mappedResponse = ChatMessageResponse.builder()
                .id("message-1")
                .conversationId("conversation-1")
                .build();

        when(currentUserProvider.getCurrentUserId()).thenReturn("user-1");
        when(conversationRepository.findById("conversation-1")).thenReturn(Optional.of(conversation));
        when(profileQueryPort.getProfileByUserId("user-1")).thenReturn(profile);
        when(chatMessageMapper.toChatMessage(request)).thenReturn(mappedMessage);
        when(chatMessageRepository.save(mappedMessage)).thenReturn(savedMessage);
        when(chatMessageMapper.toChatMessageResponse(savedMessage)).thenReturn(mappedResponse);

        ChatMessageResponse result = chatMessageService.create(request);

        assertThat(result).isSameAs(mappedResponse);
        assertThat(mappedMessage.getSender().getUserId()).isEqualTo("user-1");
        verify(profileQueryPort).getProfileByUserId("user-1");
    }
}
