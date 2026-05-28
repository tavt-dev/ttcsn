package com.friendify.app.post.port;

import static org.assertj.core.api.Assertions.assertThat;

import com.friendify.app.interaction.port.PostQueryPort;
import com.friendify.app.post.service.PostService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class PostQueryPortBeanTests {

    @Autowired
    PostQueryPort postQueryPort;

    @Test
    void postQueryPortIsBackedByPostService() {
        assertThat(postQueryPort).isInstanceOf(PostService.class);
    }
}
