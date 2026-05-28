package com.friendify.app.chat.repository;

import java.util.List;
import java.util.Optional;

import com.friendify.app.chat.entity.ReadReceipt;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ReadReceiptRepository extends JpaRepository<ReadReceipt, String> {
    Optional<ReadReceipt> findByMessageIdAndUserId(String messageId, String userId);

    List<ReadReceipt> findAllByMessageId(String messageId);

    List<ReadReceipt> findAllByConversationIdAndUserId(String conversationId, String userId);
}
