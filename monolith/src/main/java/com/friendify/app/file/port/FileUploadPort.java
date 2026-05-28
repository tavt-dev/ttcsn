package com.friendify.app.file.port;

import java.io.IOException;
import java.util.List;

import com.friendify.app.file.dto.UploadResponse;
import com.friendify.app.shared.media.ImageType;
import org.springframework.lang.Nullable;
import org.springframework.web.multipart.MultipartFile;

public interface FileUploadPort {
    UploadResponse uploadImage(MultipartFile file, ImageType imageType, String ownerId, @Nullable String postId)
            throws IOException;

    List<UploadResponse> uploadImages(
            List<MultipartFile> files,
            ImageType imageType,
            String ownerId,
            @Nullable String postId)
            throws IOException;
}
