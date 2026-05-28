package com.friendify.app.file.config;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Test;

class CloudinaryConfigTests {

    @Test
    void cloudinaryBeanFailsClearlyWhenConfigIsMissing() {
        CloudinaryConfig config = new CloudinaryConfig();

        assertThatThrownBy(() -> config.cloudinary("", "api-key", "api-secret"))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Cloudinary is not configured");
    }
}
