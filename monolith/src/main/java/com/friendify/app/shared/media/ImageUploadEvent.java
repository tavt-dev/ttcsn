package com.friendify.app.shared.media;

import java.util.List;
import java.util.Map;

public record ImageUploadEvent(
        List<String> files,
        ImageType imageType,
        String ownerId,
        String postId,
        Map<String, Object>[] propertiesMap) {
}
