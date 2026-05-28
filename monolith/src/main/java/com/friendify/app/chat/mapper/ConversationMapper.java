package com.friendify.app.chat.mapper;

import com.friendify.app.chat.dto.response.ConversationResponse;
import com.friendify.app.chat.entity.Conversation;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface ConversationMapper {
    ConversationResponse toConversationResponse(Conversation conversation);
}
