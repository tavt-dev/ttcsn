package com.friendify.app.interaction.dto.request;

import jakarta.validation.constraints.AssertTrue;
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
@FieldDefaults(level = AccessLevel.PRIVATE)
public class CreateLikeRequest {
    String postId;
    String commentId;

    @AssertTrue(message = "Must provide exactly one of postId or commentId")
    private boolean isValid() {
        return (postId != null && !postId.trim().isEmpty())
                != (commentId != null && !commentId.trim().isEmpty());
    }
}
