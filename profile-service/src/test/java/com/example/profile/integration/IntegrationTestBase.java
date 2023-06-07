package com.example.profile.integration;

import com.example.profile.integration.annotation.IT;
import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.BeforeAll;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.FixedHostPortGenericContainer;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.MongoDBContainer;
import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.utility.DockerImageName;

@IT
@RequiredArgsConstructor
public class IntegrationTestBase {

    private static final MongoDBContainer mongoDBContainer = new MongoDBContainer(DockerImageName.parse("mongo:jammy"));
    private static final GenericContainer<?> redisContainer = new GenericContainer<>(DockerImageName.parse("redis:7.2-rc-alpine3.18")).withExposedPorts(6379);
    private static final GenericContainer<?> configServerContainer = new FixedHostPortGenericContainer<>("twitterclone0/spring-cloud-config-server")
            .withFixedExposedPort(8888, 8888)
            .waitingFor(Wait.forHttp("/profile-service/test")
                    .forStatusCodeMatching(port -> port >= 200 && port < 400));

    @BeforeAll
    public static void runContainer() {
        configServerContainer.start();
        mongoDBContainer.start();
        redisContainer.start();
    }

    @DynamicPropertySource
    static void mongoProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.data.mongodb.uri", mongoDBContainer::getReplicaSetUrl);
        registry.add("spring.redis.host", redisContainer::getHost);
        registry.add("spring.redis.port", redisContainer::getFirstMappedPort);
    }
}
