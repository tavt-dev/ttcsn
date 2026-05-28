package com.friendify.app.notification.email;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import com.friendify.app.auth.port.NotificationDeliveryPort;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class NotificationDeliveryPortBeanTests {

    @Autowired
    List<NotificationDeliveryPort> notificationDeliveryPorts;

    @Test
    void notificationDeliveryPortHasExactlyOneRealEmailAdapter() {
        assertThat(notificationDeliveryPorts)
                .hasSize(1)
                .first()
                .isInstanceOf(AuthEmailNotificationAdapter.class);
    }
}
