package com.friendify.app.chat.dto.request;

import java.util.List;

import com.friendify.app.chat.enums.TypeConversation;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
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
public class ConversationRequest {
    TypeConversation typeConversation;

    @Size(min = 1)
    @NotNull
    List<String> participantIds;

    String conversationName;
}
