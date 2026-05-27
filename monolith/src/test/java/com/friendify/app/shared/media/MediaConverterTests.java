package com.friendify.app.shared.media;

import static org.assertj.core.api.Assertions.assertThat;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

import org.junit.jupiter.api.Test;

class MediaConverterTests {

    @Test
    void decodeFromBase64() {
        String encoded = Base64.getEncoder().encodeToString("friendify".getBytes(StandardCharsets.UTF_8));

        byte[] decoded = MediaConverter.decodeFromBase64(encoded);

        assertThat(new String(decoded, StandardCharsets.UTF_8)).isEqualTo("friendify");
    }
}
