package com.friendify.app.file.dto;

import java.time.Instant;

import com.friendify.app.shared.media.ImageType;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ImageResponse {
    String secureUrl;
    String publicId;
    String url;
    String thumbnailUrl;
    Instant width;
    Instant height;
    String format;
    Long size;
    String contentType;
    String ownerId;
    String postId;
    ImageType imageType;
    Instant createdDate;
}
