package com.friendify.app.post.dto.response;

import java.time.Instant;
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
public class PostResponse {
    String id;
    String content;
    String userId;
    String username;
    String userAvatar;
    String created;
    List<String> imageUrls;
    PrivacyType privacy;
    Instant createdDate;
    Instant modifiedDate;
    String groupId;
    String groupName;
    Integer likeCount;
    Integer commentCount;
    Boolean isLiked;
    Boolean isSaved;
    Boolean isOwnerPost;
    Long shareCount;
    String originalPostUserId;
    String originalPostUsername;
    String originalPostUserAvatar;
}
