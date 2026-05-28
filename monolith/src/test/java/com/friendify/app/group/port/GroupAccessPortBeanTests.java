package com.friendify.app.group.port;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class GroupAccessPortBeanTests {

    @Autowired
    GroupAccessPort groupAccessPort;

    @Test
    void groupAccessPortBeanExists() {
        assertThat(groupAccessPort).isNotNull();
    }
}
