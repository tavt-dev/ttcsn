package com.friendify.app.shared.media;

import java.util.List;

public record MultipleImageResponse(List<ImageUploadedEvent> uploadedEvents) {
}
