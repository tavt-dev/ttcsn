package com.friendify.app.interaction.port;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class InteractionPortBeanTests {

    @Autowired
    InteractionQueryPort interactionQueryPort;

    @Autowired
    InteractionCleanupPort interactionCleanupPort;

    @Autowired
    PostQueryPort postQueryPort;

    @Test
    void interactionPortsExist() {
        assertThat(interactionQueryPort).isNotNull();
        assertThat(interactionCleanupPort).isNotNull();
        assertThat(postQueryPort).isNotNull();
    }
}
