package com.friendify.app.shared.media;

import java.util.Map;

public record ImageUploadedEvent(
        String publicId,
        String imageUrl,
        Map<String, Object> properties) {

    public ImageUploadedEvent {
        properties = properties == null ? Map.of() : Map.copyOf(properties);
    }

    public <T> T getProperty(String id, Class<T> type) {
        Object value = properties.get(id);
        return type.isInstance(value) ? type.cast(value) : null;
    }

    @SuppressWarnings("unchecked")
    public <T> T getProperty(String id) {
        return (T) properties.get(id);
    }
}
