package com.friendify.app.post.dto.request;

import java.util.List;

import com.friendify.app.post.enums.PrivacyType;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class PostRequest {
    String content;
    List<String> imageUrls;
    PrivacyType privacy;
    String groupId;
}
