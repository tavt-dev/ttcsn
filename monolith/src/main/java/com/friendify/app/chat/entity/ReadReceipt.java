package com.friendify.app.chat.entity;

import java.time.Instant;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.FieldDefaults;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(
        name = "read_receipt",
        uniqueConstraints = @UniqueConstraint(name = "uk_read_receipt_message_user", columnNames = {"message_id", "user_id"}))
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ReadReceipt {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    String id;

    @Column(name = "message_id")
    String messageId;

    @Column(name = "conversation_id")
    String conversationId;

    @Column(name = "user_id")
    String userId;

    Instant readAt;

    @PrePersist
    void prePersist() {
        readAt = readAt == null ? Instant.now() : readAt;
    }
}
