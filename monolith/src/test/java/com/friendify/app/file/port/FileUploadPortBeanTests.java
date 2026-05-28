package com.friendify.app.file.port;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class FileUploadPortBeanTests {

    @Autowired
    FileUploadPort fileUploadPort;

    @Test
    void fileUploadPortBeanExists() {
        assertThat(fileUploadPort).isNotNull();
    }
}
