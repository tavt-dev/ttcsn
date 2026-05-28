package com.friendify.app.chat.service;

import java.time.Instant;
import java.util.List;

import com.friendify.app.chat.dto.request.ChatMessageRequest;
import com.friendify.app.chat.dto.request.UpdateMessageRequest;
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
import com.friendify.app.shared.dto.PageResponse;
import com.friendify.app.shared.exception.AppException;
import com.friendify.app.shared.exception.ErrorCode;
import com.friendify.app.shared.security.CurrentUserProvider;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
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
public class ChatMessageService {

    static String SORT_FIELD_CREATED_DATE = "createdDate";

    ChatMessageRepository chatMessageRepository;
    ChatMessageMapper chatMessageMapper;
    ProfileQueryPort profileQueryPort;
    ConversationRepository conversationRepository;
    CurrentUserProvider currentUserProvider;

    public ChatMessageResponse create(ChatMessageRequest request) {
        String userId = currentUserProvider.getCurrentUserId();
        Conversation conversation = validateConversationAccess(request.getConversationId(), userId);
        ProfileResponse userInfo = getProfileOrThrow(userId);
        ChatMessage chatMessage = buildChatMessage(request, userInfo, conversation, userId);
        chatMessage = chatMessageRepository.save(chatMessage);
        return toChatMessageResponse(chatMessage, userId);
    }

    public List<ChatMessageResponse> getMessages(String conversationId) {
        String userId = currentUserProvider.getCurrentUserId();
        validateConversationAccess(conversationId, userId);
        return chatMessageRepository.findAllByConversationIdOrderByCreatedDateDesc(conversationId).stream()
                .map(message -> toChatMessageResponse(message, userId))
                .toList();
    }

    public PageResponse<ChatMessageResponse> getMessagesWithPagination(String conversationId, int page, int size) {
        String userId = currentUserProvider.getCurrentUserId();
        validateConversationAccess(conversationId, userId);

        Pageable pageable = PageRequest.of(page - 1, size, Sort.by(SORT_FIELD_CREATED_DATE).descending());
        Page<ChatMessage> messagePage =
                chatMessageRepository.findAllByConversationIdOrderByCreatedDateDesc(conversationId, pageable);
        List<ChatMessageResponse> responses = messagePage.getContent().stream()
                .map(message -> toChatMessageResponse(message, userId))
                .toList();

        return PageResponse.<ChatMessageResponse>builder()
                .content(responses)
                .page(page)
                .size(size)
                .totalElements(messagePage.getTotalElements())
                .totalPages(messagePage.getTotalPages())
                .hasNext(messagePage.hasNext())
                .hasPrevious(messagePage.hasPrevious())
                .build();
    }

    public ChatMessageResponse getById(String messageId) {
        String userId = currentUserProvider.getCurrentUserId();
        ChatMessage message = chatMessageRepository.findById(messageId)
                .orElseThrow(() -> new AppException(ErrorCode.MESSAGE_NOT_FOUND));
        validateConversationAccess(message.getConversationId(), userId);
        return toChatMessageResponse(message, userId);
    }

    @Transactional
    public ChatMessageResponse update(String messageId, UpdateMessageRequest request) {
        String userId = currentUserProvider.getCurrentUserId();
        ChatMessage message = findMessageOrThrow(messageId);
        validateSender(message, userId);
        validateConversationAccess(message.getConversationId(), userId);

        message.setMessage(request.getMessage());
        return toChatMessageResponse(chatMessageRepository.save(message), userId);
    }

    @Transactional
    public void delete(String messageId) {
        String userId = currentUserProvider.getCurrentUserId();
        ChatMessage message = findMessageOrThrow(messageId);
        validateDeletePermission(message, userId);
        validateConversationAccess(message.getConversationId(), userId);
        chatMessageRepository.delete(message);
    }

    private ChatMessage findMessageOrThrow(String messageId) {
        return chatMessageRepository.findById(messageId)
                .orElseThrow(() -> new AppException(ErrorCode.MESSAGE_NOT_FOUND));
    }

    private ProfileResponse getProfileOrThrow(String userId) {
        try {
            return profileQueryPort.getProfileByUserId(userId);
        } catch (RuntimeException exception) {
            throw new AppException(ErrorCode.USER_NOT_EXISTED);
        }
    }

    private ChatMessage buildChatMessage(
            ChatMessageRequest request, ProfileResponse userInfo, Conversation conversation, String userId) {
        ChatMessage chatMessage = chatMessageMapper.toChatMessage(request);
        ParticipantRole senderRole = getSenderRoleFromConversation(conversation, userId);
        chatMessage.setSender(buildParticipantInfo(userInfo, senderRole));
        chatMessage.setCreatedDate(Instant.now());
        return chatMessage;
    }

    private ParticipantRole getSenderRoleFromConversation(Conversation conversation, String userId) {
        return conversation.getParticipants().stream()
                .filter(participant -> userId.equals(participant.getUserId()))
                .findFirst()
                .map(ParticipantInfo::getRole)
                .orElse(ParticipantRole.MEMBER);
    }

    private ParticipantInfo buildParticipantInfo(ProfileResponse profile, ParticipantRole role) {
        return ParticipantInfo.builder()
                .userId(profile.getUserId())
                .username(profile.getUsername())
                .firstName(profile.getFirstName())
                .lastName(profile.getLastName())
                .avatar(profile.getAvatar())
                .role(role)
                .build();
    }

    private void validateSender(ChatMessage message, String userId) {
        if (message == null) {
            throw new AppException(ErrorCode.MESSAGE_NOT_FOUND);
        }
        if (message.getSender() == null || !userId.equals(message.getSender().getUserId())) {
            throw new AppException(ErrorCode.UNAUTHORIZED);
        }
    }

    private void validateDeletePermission(ChatMessage message, String userId) {
        if (message == null) {
            throw new AppException(ErrorCode.MESSAGE_NOT_FOUND);
        }
        if (message.getSender() == null) {
            throw new AppException(ErrorCode.UNAUTHORIZED);
        }
        if (userId.equals(message.getSender().getUserId())) {
            return;
        }

        Conversation conversation = conversationRepository.findById(message.getConversationId())
                .orElseThrow(() -> new AppException(ErrorCode.CONVERSATION_NOT_FOUND));
        boolean isAdmin = conversation.getParticipants().stream()
                .anyMatch(participant -> userId.equals(participant.getUserId())
                        && participant.getRole() == ParticipantRole.ADMIN);
        if (!isAdmin) {
            throw new AppException(ErrorCode.UNAUTHORIZED);
        }
    }

    Conversation validateConversationAccess(String conversationId, String userId) {
        if (userId == null || userId.trim().isEmpty()) {
            throw new AppException(ErrorCode.USER_ID_REQUIRED);
        }
        Conversation conversation = conversationRepository.findById(conversationId)
                .orElseThrow(() -> new AppException(ErrorCode.CONVERSATION_NOT_FOUND));
        boolean hasAccess = conversation.getParticipants().stream()
                .anyMatch(participant -> userId.equals(participant.getUserId()));
        if (!hasAccess) {
            throw new AppException(ErrorCode.CONVERSATION_NOT_FOUND);
        }
        return conversation;
    }

    private ChatMessageResponse toChatMessageResponse(ChatMessage chatMessage, String userId) {
        ChatMessageResponse response = chatMessageMapper.toChatMessageResponse(chatMessage);
        response.setMe(userId != null && userId.equals(chatMessage.getSender().getUserId()));
        return response;
    }
}
