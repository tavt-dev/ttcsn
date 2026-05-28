package com.friendify.app.chat.mapper;

import com.friendify.app.chat.dto.request.ChatMessageRequest;
import com.friendify.app.chat.dto.response.ChatMessageResponse;
import com.friendify.app.chat.entity.ChatMessage;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface ChatMessageMapper {
    ChatMessageResponse toChatMessageResponse(ChatMessage chatMessage);

    ChatMessage toChatMessage(ChatMessageRequest request);
}
