package com.example.tweets.integration;

import com.example.tweets.integration.annotation.IT;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.FixedHostPortGenericContainer;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.utility.DockerImageName;

@IT
public class IntegrationTestBase {

    @Container
    private static final PostgreSQLContainer<?> postgresContainer = new PostgreSQLContainer<>(DockerImageName.parse("postgres:alpine3.17"));
    @Container
    private static final GenericContainer<?> redisContainer = new GenericContainer<>(DockerImageName.parse("redis:7.2-rc-alpine3.18")).withExposedPorts(6379);
    @Container
    private static final GenericContainer<?> configServerContainer = new FixedHostPortGenericContainer<>("twitterclone0/spring-cloud-config-server")
            .withFixedExposedPort(8888, 8888)
            .waitingFor(Wait.forHttp("/tweet-service/test")
                    .forStatusCodeMatching(port -> port >= 200 && port < 400));

    @DynamicPropertySource
    static void dataSourcesProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgresContainer::getJdbcUrl);
        registry.add("spring.redis.host", redisContainer::getHost);
        registry.add("spring.redis.port", redisContainer::getFirstMappedPort);
    }
}
