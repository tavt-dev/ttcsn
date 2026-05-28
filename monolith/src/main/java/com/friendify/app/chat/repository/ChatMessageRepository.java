package com.friendify.app.chat.repository;

import java.util.List;
import java.util.Optional;

import com.friendify.app.chat.entity.ChatMessage;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ChatMessageRepository extends JpaRepository<ChatMessage, String> {
    List<ChatMessage> findAllByConversationIdOrderByCreatedDateDesc(String conversationId);

    Page<ChatMessage> findAllByConversationIdOrderByCreatedDateDesc(String conversationId, Pageable pageable);

    Optional<ChatMessage> findByIdAndConversationId(String id, String conversationId);
}
