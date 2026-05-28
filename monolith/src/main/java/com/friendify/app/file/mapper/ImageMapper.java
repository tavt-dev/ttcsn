package com.friendify.app.file.mapper;

import com.friendify.app.file.dto.UploadResponse;
import com.friendify.app.file.entity.Image;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface ImageMapper {
    @Mapping(target = "version", expression = "java(image.getVersion() != null ? Long.parseLong(image.getVersion()) : null)")
    UploadResponse toUploadResponse(Image image);
}
