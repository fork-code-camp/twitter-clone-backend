package com.example.fanout;

import com.example.fanout.annotation.IT;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.FixedHostPortGenericContainer;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.utility.DockerImageName;

@IT
@DirtiesContext
public class IntegrationTestBase {

    @Container
    @SuppressWarnings("resource")
    private static final GenericContainer<?> redisContainer = new GenericContainer<>(DockerImageName.parse("redis:7.2-rc-alpine3.18")).withExposedPorts(6379);
    @Container
    @SuppressWarnings({"resource", "unused", "deprecation"})
    private static final GenericContainer<?> configServerContainer = new FixedHostPortGenericContainer<>(
            "twitterclone0/twitter-spring-cloud-config-server:2.0.1"
    )
            .withFixedExposedPort(8888, 8888)
            .waitingFor(Wait.forHttp("/fanout-service/test")
                    .forStatusCodeMatching(port -> port >= 200 && port < 400));

    @DynamicPropertySource
    static void dataSourcesProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.data.redis.host", redisContainer::getHost);
        registry.add("spring.data.redis.port", redisContainer::getFirstMappedPort);
    }
}
