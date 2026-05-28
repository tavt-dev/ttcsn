package com.friendify.app.file.entity;

import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Embeddable
@Data
@AllArgsConstructor
@NoArgsConstructor
public class ImageVersions {
    private String thumbnail;
    private String medium;
    private String large;
    private String original;
}
