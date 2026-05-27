package com.friendify.app.notification.email;

import static org.assertj.core.api.Assertions.assertThat;

import com.friendify.app.auth.port.NotificationDeliveryPort;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class NotificationDeliveryPortBeanTests {

    @Autowired
    NotificationDeliveryPort notificationDeliveryPort;

    @Test
    void notificationDeliveryPortUsesRealEmailAdapter() {
        assertThat(notificationDeliveryPort).isInstanceOf(AuthEmailNotificationAdapter.class);
    }
}
