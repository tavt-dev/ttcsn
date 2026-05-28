package com.friendify.app.chat.service;

import java.time.Instant;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.friendify.app.chat.dto.response.ReadReceiptResponse;
import com.friendify.app.chat.entity.ChatMessage;
import com.friendify.app.chat.entity.ReadReceipt;
import com.friendify.app.chat.mapper.ReadReceiptMapper;
import com.friendify.app.chat.repository.ChatMessageRepository;
import com.friendify.app.chat.repository.ConversationRepository;
import com.friendify.app.chat.repository.ReadReceiptRepository;
import com.friendify.app.shared.exception.AppException;
import com.friendify.app.shared.exception.ErrorCode;
import com.friendify.app.shared.security.CurrentUserProvider;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class ReadReceiptService {

    ReadReceiptRepository readReceiptRepository;
    ChatMessageRepository chatMessageRepository;
    ConversationRepository conversationRepository;
    ReadReceiptMapper readReceiptMapper;
    CurrentUserProvider currentUserProvider;

    @Transactional
    public ReadReceiptResponse markAsRead(String messageId) {
        String userId = currentUserProvider.getCurrentUserId();
        ChatMessage message = findMessageOrThrow(messageId);
        validateConversationAccess(message.getConversationId());
        validateNotSender(message, userId);

        ReadReceipt receipt = readReceiptRepository.findByMessageIdAndUserId(messageId, userId)
                .orElseGet(() -> createReadReceipt(messageId, message.getConversationId(), userId));
        return readReceiptMapper.toReadReceiptResponse(receipt);
    }

    public List<ReadReceiptResponse> getReadReceipts(String messageId) {
        ChatMessage message = findMessageOrThrow(messageId);
        validateConversationAccess(message.getConversationId());
        return readReceiptRepository.findAllByMessageId(messageId).stream()
                .map(readReceiptMapper::toReadReceiptResponse)
                .toList();
    }

    public long getUnreadCount(String conversationId) {
        String userId = currentUserProvider.getCurrentUserId();
        validateConversationAccess(conversationId);

        List<ChatMessage> allMessages =
                chatMessageRepository.findAllByConversationIdOrderByCreatedDateDesc(conversationId);
        Set<String> readMessageIds = new HashSet<>(
                readReceiptRepository.findAllByConversationIdAndUserId(conversationId, userId).stream()
                        .map(ReadReceipt::getMessageId)
                        .toList());

        return allMessages.stream()
                .filter(message -> !message.getSender().getUserId().equals(userId))
                .filter(message -> !readMessageIds.contains(message.getId()))
                .count();
    }

    private ChatMessage findMessageOrThrow(String messageId) {
        return chatMessageRepository.findById(messageId)
                .orElseThrow(() -> new AppException(ErrorCode.MESSAGE_NOT_FOUND));
    }

    private ReadReceipt createReadReceipt(String messageId, String conversationId, String userId) {
        return readReceiptRepository.save(ReadReceipt.builder()
                .messageId(messageId)
                .conversationId(conversationId)
                .userId(userId)
                .readAt(Instant.now())
                .build());
    }

    private void validateNotSender(ChatMessage message, String userId) {
        if (message.getSender().getUserId().equals(userId)) {
            throw new AppException(ErrorCode.UNAUTHORIZED);
        }
    }

    private void validateConversationAccess(String conversationId) {
        String userId = currentUserProvider.getCurrentUserId();
        conversationRepository.findById(conversationId)
                .orElseThrow(() -> new AppException(ErrorCode.CONVERSATION_NOT_FOUND))
                .getParticipants()
                .stream()
                .filter(participant -> userId.equals(participant.getUserId()))
                .findAny()
                .orElseThrow(() -> new AppException(ErrorCode.CONVERSATION_NOT_FOUND));
    }
}
