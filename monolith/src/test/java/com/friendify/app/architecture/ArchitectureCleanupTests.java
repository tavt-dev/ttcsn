package com.friendify.app.architecture;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Stream;

import com.friendify.app.auth.port.NotificationDeliveryPort;
import com.friendify.app.file.port.FileUploadPort;
import com.friendify.app.group.port.GroupAccessPort;
import com.friendify.app.interaction.port.InteractionCleanupPort;
import com.friendify.app.interaction.port.InteractionQueryPort;
import com.friendify.app.interaction.port.PostQueryPort;
import com.friendify.app.profile.port.ProfileCreationPort;
import com.friendify.app.profile.port.ProfileQueryPort;
import com.friendify.app.social.port.SocialGraphQueryPort;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class ArchitectureCleanupTests {

    @Autowired
    ProfileCreationPort profileCreationPort;

    @Autowired
    ProfileQueryPort profileQueryPort;

    @Autowired
    NotificationDeliveryPort notificationDeliveryPort;

    @Autowired
    FileUploadPort fileUploadPort;

    @Autowired
    SocialGraphQueryPort socialGraphQueryPort;

    @Autowired
    GroupAccessPort groupAccessPort;

    @Autowired
    InteractionQueryPort interactionQueryPort;

    @Autowired
    InteractionCleanupPort interactionCleanupPort;

    @Autowired
    PostQueryPort postQueryPort;

    @Test
    void directModulePortsAreWired() {
        assertThat(profileCreationPort).isNotNull();
        assertThat(profileQueryPort).isNotNull();
        assertThat(notificationDeliveryPort).isNotNull();
        assertThat(fileUploadPort).isNotNull();
        assertThat(socialGraphQueryPort).isNotNull();
        assertThat(groupAccessPort).isNotNull();
        assertThat(interactionQueryPort).isNotNull();
        assertThat(interactionCleanupPort).isNotNull();
        assertThat(postQueryPort).isNotNull();
    }

    @Test
    void monolithRuntimeDoesNotContainObsoleteMigrationDependencies() throws IOException {
        List<String> forbiddenMarkers = List.of(
                "com.tien",
                "KafkaTemplate",
                "@KafkaListener",
                "ImageUploadKafkaService",
                "ImageTopics",
                "spring-kafka",
                "spring-cloud-starter-openfeign",
                "spring-cloud-config",
                "spring-cloud-starter-gateway",
                "MongoRepository",
                "@Document",
                "spring.data.mongodb",
                "data-mongodb",
                "RedisTemplate",
                "StringRedisTemplate");

        String runtimeText = readRuntimeFiles();

        assertThat(forbiddenMarkers)
                .filteredOn(runtimeText::contains)
                .isEmpty();
    }

    private String readRuntimeFiles() throws IOException {
        Path projectRoot = Path.of("").toAbsolutePath();
        StringBuilder combined = new StringBuilder(Files.readString(projectRoot.resolve("pom.xml"), StandardCharsets.UTF_8));
        for (Path root : List.of(projectRoot.resolve("src/main/java"), projectRoot.resolve("src/main/resources"))) {
            if (!Files.exists(root)) {
                continue;
            }
            try (Stream<Path> paths = Files.walk(root)) {
                paths.filter(Files::isRegularFile)
                        .forEach(path -> appendFile(combined, path));
            }
        }
        return combined.toString();
    }

    private void appendFile(StringBuilder combined, Path path) {
        try {
            combined.append('\n').append(Files.readString(path, StandardCharsets.UTF_8));
        } catch (IOException e) {
            throw new IllegalStateException("Cannot read " + path, e);
        }
    }
}
