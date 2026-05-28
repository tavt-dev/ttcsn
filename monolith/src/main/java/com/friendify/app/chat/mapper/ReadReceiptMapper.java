package com.friendify.app.chat.mapper;

import com.friendify.app.chat.dto.response.ReadReceiptResponse;
import com.friendify.app.chat.entity.ReadReceipt;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface ReadReceiptMapper {
    ReadReceiptResponse toReadReceiptResponse(ReadReceipt readReceipt);
}
