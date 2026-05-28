package com.friendify.app.file.service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.cloudinary.Cloudinary;
import com.cloudinary.Transformation;
import com.cloudinary.utils.ObjectUtils;
import com.friendify.app.file.dto.UploadResponse;
import com.friendify.app.file.entity.Image;
import com.friendify.app.file.entity.ImageVersions;
import com.friendify.app.file.mapper.ImageMapper;
import com.friendify.app.file.port.FileUploadPort;
import com.friendify.app.file.repository.ImageRepository;
import com.friendify.app.shared.exception.AppException;
import com.friendify.app.shared.exception.ErrorCode;
import com.friendify.app.shared.media.ImageType;
import com.friendify.app.shared.media.ImageUploadEvent;
import com.friendify.app.shared.media.ImageUploadedEvent;
import com.friendify.app.shared.media.MultipleImageResponse;
import com.friendify.app.shared.media.MediaConverter;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.experimental.FieldDefaults;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

@Service
@RequiredArgsConstructor
@Slf4j
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class ImageService implements FileUploadPort {

    static final Set<String> ALLOWED_CONTENT_TYPES = Set.of(
            "image/jpeg", "image/png", "image/webp", "image/gif", "image/avif");
    static final long MAX_FILE_SIZE = 20 * 1024 * 1024;

    Cloudinary cloudinary;
    ImageRepository imageRepository;
    ImageMapper imageMapper;

    @Transactional
    public MultipleImageResponse uploadImages(ImageUploadEvent event) {
        validateEvent(event);

        List<ImageUploadedEvent> uploadedEvents = new ArrayList<>();
        try {
            for (int i = 0; i < event.files().size(); i++) {
                Map<String, Object> props = null;
                if (event.propertiesMap() != null && i < event.propertiesMap().length) {
                    props = event.propertiesMap()[i];
                }
                uploadedEvents.add(uploadImage(
                        event.files().get(i),
                        event.imageType(),
                        event.ownerId(),
                        event.postId(),
                        props));
            }
        } catch (AppException exception) {
            throw exception;
        } catch (Exception exception) {
            log.error("Upload multiple images failed", exception);
            throw new AppException(ErrorCode.CLOUDINARY_UPLOAD_FAILED);
        }

        return new MultipleImageResponse(uploadedEvents);
    }

    @Transactional
    public ImageUploadedEvent uploadImage(ImageUploadEvent event) throws IOException {
        validateEvent(event);

        Map<String, Object> props = null;
        if (event.propertiesMap() != null && event.propertiesMap().length > 0) {
            props = event.propertiesMap()[0];
        }

        return uploadImage(event.files().get(0), event.imageType(), event.ownerId(), event.postId(), props);
    }

    private ImageUploadedEvent uploadImage(
            String base64,
            ImageType imageType,
            String ownerId,
            String postId,
            Map<String, Object> properties)
            throws IOException {
        if (!StringUtils.hasText(base64)) {
            throw new AppException(ErrorCode.FILE_EMPTY);
        }

        byte[] bytes = MediaConverter.decodeFromBase64(base64);
        if (bytes.length > MAX_FILE_SIZE) {
            throw new AppException(ErrorCode.FILE_TOO_LARGE);
        }

        Image image = saveUploadedBytes(
                bytes,
                imageType,
                ownerId,
                postId,
                null);
        Map<String, Object> safeProps = properties == null ? Map.of() : Map.copyOf(properties);
        return new ImageUploadedEvent(image.getPublicId(), image.getSecureUrl(), safeProps);
    }

    @Override
    @Transactional
    public List<UploadResponse> uploadImages(
            List<MultipartFile> files,
            ImageType imageType,
            String ownerId,
            @Nullable String postId)
            throws IOException {
        if (files == null || files.isEmpty()) {
            throw new AppException(ErrorCode.FILE_EMPTY);
        }

        List<UploadResponse> responses = new ArrayList<>();
        for (MultipartFile file : files) {
            responses.add(uploadImage(file, imageType, ownerId, postId));
        }
        return responses;
    }

    @Override
    @Transactional
    public UploadResponse uploadImage(
            MultipartFile file,
            ImageType imageType,
            String ownerId,
            @Nullable String postId)
            throws IOException {
        validateMultipartFile(file, imageType, ownerId, postId);
        Image image = saveUploadedBytes(
                file.getBytes(),
                imageType,
                ownerId,
                postId,
                file.getContentType());
        return imageMapper.toUploadResponse(image);
    }

    private void validateEvent(ImageUploadEvent event) {
        if (event == null || event.files() == null || event.files().isEmpty()) {
            throw new AppException(ErrorCode.FILE_EMPTY);
        }
        validateOwnerAndPost(event.imageType(), event.ownerId(), event.postId());
    }

    private void validateMultipartFile(
            MultipartFile file,
            ImageType imageType,
            String ownerId,
            @Nullable String postId) {
        if (file == null || file.isEmpty()) {
            throw new AppException(ErrorCode.FILE_EMPTY);
        }
        validateOwnerAndPost(imageType, ownerId, postId);
        if (!ALLOWED_CONTENT_TYPES.contains(file.getContentType())) {
            throw new AppException(ErrorCode.FILE_TYPE_NOT_ALLOWED);
        }
        if (file.getSize() > MAX_FILE_SIZE) {
            throw new AppException(ErrorCode.FILE_TOO_LARGE);
        }
    }

    private void validateOwnerAndPost(ImageType imageType, String ownerId, @Nullable String postId) {
        if (!StringUtils.hasText(ownerId)) {
            throw new AppException(ErrorCode.OWNER_ID_REQUIRED);
        }
        if (imageType == ImageType.POST_IMAGE && !StringUtils.hasText(postId)) {
            throw new AppException(ErrorCode.POST_ID_REQUIRED);
        }
    }

    private Image saveUploadedBytes(
            byte[] bytes,
            ImageType imageType,
            String ownerId,
            @Nullable String postId,
            @Nullable String contentType) {
        final String folder = buildFolder(imageType, ownerId, postId);
        Map<String, Object> options = ObjectUtils.asMap(
                "folder", folder,
                "resource_type", "image",
                "unique_filename", true,
                "overwrite", false,
                "invalidate", true,
                "quality", "auto",
                "fetch_format", "auto",
                "use_filename", false);

        final Map<?, ?> uploadResult;
        try {
            uploadResult = cloudinary.uploader().upload(bytes, options);
        } catch (Exception exception) {
            throw new AppException(ErrorCode.CLOUDINARY_UPLOAD_FAILED);
        }

        String publicId = uploadResult.get("public_id").toString();
        String format = uploadResult.get("format") != null ? uploadResult.get("format").toString() : null;
        String resolvedContentType = StringUtils.hasText(contentType)
                ? contentType
                : format == null ? "image/jpeg" : "image/" + format.toLowerCase();

        Image image = Image.builder()
                .ownerId(ownerId)
                .postId(postId)
                .contentType(resolvedContentType)
                .size((long) bytes.length)
                .imageType(imageType)
                .secureUrl(uploadResult.get("secure_url").toString())
                .publicId(publicId)
                .format(format)
                .width((Integer) uploadResult.get("width"))
                .height((Integer) uploadResult.get("height"))
                .imageVersions(generateImageVersions(publicId))
                .version(uploadResult.get("version") == null ? null : uploadResult.get("version").toString())
                .build();

        Image savedImage = imageRepository.save(image);
        log.info("Saved image metadata with id: {}", savedImage.getId());
        return savedImage;
    }

    private String buildUrl(String publicId, int width, int height, String crop) {
        return cloudinary
                .url()
                .transformation(new Transformation().width(width).height(height).crop(crop))
                .secure(true)
                .generate(publicId);
    }

    private ImageVersions generateImageVersions(String publicId) {
        return new ImageVersions(
                buildUrl(publicId, 150, 150, "fill"),
                buildUrl(publicId, 500, 500, "limit"),
                buildUrl(publicId, 1200, 1200, "limit"),
                cloudinary.url().secure(true).generate(publicId));
    }

    private String buildFolder(ImageType imageType, String ownerId, @Nullable String postId) {
        return switch (imageType) {
            case AVATAR -> "avatars/%s".formatted(ownerId);
            case POST_IMAGE -> "posts/%s/%s".formatted(ownerId, postId);
            case BACKGROUND_IMAGE -> "backgrounds/%s".formatted(ownerId);
            case GROUP_AVATAR -> "groups/%s/avatar".formatted(ownerId);
            case GROUP_COVER -> "groups/%s/cover".formatted(ownerId);
        };
    }
}
