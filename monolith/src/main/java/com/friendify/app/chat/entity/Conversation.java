package com.friendify.app.chat.entity;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

import com.friendify.app.chat.enums.TypeConversation;
import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OrderColumn;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
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
@Table(name = "conversation")
@FieldDefaults(level = AccessLevel.PRIVATE)
public class Conversation {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    String id;

    @Enumerated(EnumType.STRING)
    TypeConversation typeConversation;

    @Column(unique = true)
    String participantsHash;

    @Builder.Default
    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "conversation_participants", joinColumns = @JoinColumn(name = "conversation_id"))
    @OrderColumn(name = "participant_order")
    List<ParticipantInfo> participants = new ArrayList<>();

    String conversationName;
    String conversationAvatar;
    Instant createdDate;
    Instant modifiedDate;

    @PrePersist
    void prePersist() {
        Instant now = Instant.now();
        createdDate = createdDate == null ? now : createdDate;
        modifiedDate = modifiedDate == null ? now : modifiedDate;
    }

    @PreUpdate
    void preUpdate() {
        modifiedDate = Instant.now();
    }
}
