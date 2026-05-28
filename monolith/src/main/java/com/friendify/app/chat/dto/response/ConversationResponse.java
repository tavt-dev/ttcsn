package com.friendify.app.chat.dto.response;

import java.time.Instant;
import java.util.List;

import com.friendify.app.chat.entity.ParticipantInfo;
import com.friendify.app.chat.enums.TypeConversation;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ConversationResponse {
    String id;
    TypeConversation typeConversation;
    String participantsHash;
    String conversationAvatar;
    String conversationName;
    List<ParticipantInfo> participants;
    Instant createdDate;
    Instant modifiedDate;
}
