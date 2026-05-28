package com.friendify.app.file.controller;

import java.io.IOException;
import java.util.List;

import com.friendify.app.file.dto.UploadResponse;
import com.friendify.app.file.service.ImageService;
import com.friendify.app.shared.media.ImageType;
import com.friendify.app.shared.media.ImageUploadEvent;
import com.friendify.app.shared.media.ImageUploadedEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/v1/file/images")
@RequiredArgsConstructor
public class CloudMediaController {

    private final ImageService imageService;

    @PostMapping("/upload")
    public ResponseEntity<ImageUploadedEvent> uploadImage(@RequestBody ImageUploadEvent imageUploadEvent)
            throws IOException {
        return ResponseEntity.ok(imageService.uploadImage(imageUploadEvent));
    }

    @PostMapping(value = "/upload-form-data", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<UploadResponse> uploadImageFormData(
            @RequestParam("file") MultipartFile file,
            @RequestParam("type") ImageType imageType,
            @RequestParam("ownerId") String ownerId,
            @RequestParam(value = "postId", required = false) String postId)
            throws IOException {
        return ResponseEntity.ok(imageService.uploadImage(file, imageType, ownerId, postId));
    }

    @PostMapping(value = "/upload-multiple-form-data", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<List<UploadResponse>> uploadMultipleImagesFormData(
            @RequestParam("files") List<MultipartFile> files,
            @RequestParam("type") ImageType imageType,
            @RequestParam("ownerId") String ownerId,
            @RequestParam(value = "postId", required = false) String postId)
            throws IOException {
        return ResponseEntity.ok(imageService.uploadImages(files, imageType, ownerId, postId));
    }
}
