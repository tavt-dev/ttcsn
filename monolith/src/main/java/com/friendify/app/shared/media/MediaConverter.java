package com.friendify.app.shared.media;

import java.io.IOException;
import java.util.Base64;
import java.util.List;

import org.springframework.web.multipart.MultipartFile;

public final class MediaConverter {

    private MediaConverter() {
    }

    public static List<String> convertToBase64(List<MultipartFile> files) {
        return files.stream()
                .map(MediaConverter::convertToBase64)
                .toList();
    }

    public static String convertToBase64(MultipartFile file) {
        try {
            return Base64.getEncoder().encodeToString(file.getBytes());
        } catch (IOException exception) {
            throw new IllegalArgumentException("Could not read multipart file", exception);
        }
    }

    public static byte[] decodeFromBase64(String base64) {
        return Base64.getDecoder().decode(base64);
    }
}
