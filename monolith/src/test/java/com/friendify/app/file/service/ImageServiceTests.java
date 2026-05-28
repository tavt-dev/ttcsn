package com.friendify.app.file.service;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;

import com.cloudinary.Cloudinary;
import com.friendify.app.file.mapper.ImageMapper;
import com.friendify.app.file.repository.ImageRepository;
import com.friendify.app.shared.exception.AppException;
import com.friendify.app.shared.exception.ErrorCode;
import com.friendify.app.shared.media.ImageType;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockMultipartFile;

class ImageServiceTests {

    @Test
    void uploadImageRejectsUnsupportedContentTypeBeforeCloudinaryCall() {
        ImageService imageService = new ImageService(
                mock(Cloudinary.class),
                mock(ImageRepository.class),
                mock(ImageMapper.class));
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "avatar.txt",
                "text/plain",
                "not-image".getBytes());

        assertThatThrownBy(() -> imageService.uploadImage(file, ImageType.AVATAR, "user-1", null))
                .isInstanceOf(AppException.class)
                .extracting(exception -> ((AppException) exception).getErrorCode())
                .isEqualTo(ErrorCode.FILE_TYPE_NOT_ALLOWED);
    }
}
