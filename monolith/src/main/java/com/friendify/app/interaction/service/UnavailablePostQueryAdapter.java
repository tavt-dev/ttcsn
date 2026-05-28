package com.friendify.app.interaction.service;

import com.friendify.app.interaction.port.PostQueryPort;
import com.friendify.app.shared.exception.AppException;
import com.friendify.app.shared.exception.ErrorCode;
import org.springframework.stereotype.Component;

@Component
public class UnavailablePostQueryAdapter implements PostQueryPort {

    @Override
    public boolean exists(String postId) {
        // TODO Step 8: replace with the real post module adapter when post is migrated.
        throw new AppException(ErrorCode.POST_MODULE_NOT_MIGRATED);
    }
}
