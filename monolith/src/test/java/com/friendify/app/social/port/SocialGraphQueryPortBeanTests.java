package com.friendify.app.social.port;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class SocialGraphQueryPortBeanTests {

    @Autowired
    SocialGraphQueryPort socialGraphQueryPort;

    @Test
    void socialGraphQueryPortBeanExists() {
        assertThat(socialGraphQueryPort).isNotNull();
    }
}
