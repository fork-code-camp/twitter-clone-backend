package com.example.tweet.integration;

import com.example.tweet.integration.annotation.IT;
import org.junit.jupiter.api.BeforeAll;
import org.springframework.http.MediaType;
import org.springframework.lang.NonNull;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.FixedHostPortGenericContainer;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.KafkaContainer;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.utility.DockerImageName;

import static com.example.tweet.integration.constants.JsonConstants.REQUEST_PATTERN;

@IT
@DirtiesContext
public class IntegrationTestBase {

    private static final PostgreSQLContainer<?> postgresContainer = new PostgreSQLContainer<>(DockerImageName.parse("postgres:alpine3.17"));
    private static final GenericContainer<?> redisContainer = new GenericContainer<>(DockerImageName.parse("redis:7.2-rc-alpine3.18")).withExposedPorts(6379);
    private static final KafkaContainer kafka = new KafkaContainer(DockerImageName.parse("confluentinc/cp-kafka:7.3.2"))
            .waitingFor(Wait.forListeningPort());
    private static final GenericContainer<?> configServerContainer = new FixedHostPortGenericContainer<>(
            "twitterclone0/twitter-spring-cloud-config-server:2.0.0"
    )
            .withFixedExposedPort(8888, 8888)
            .waitingFor(Wait.forHttp("/tweet-service/test")
                    .forStatusCodeMatching(port -> port >= 200 && port < 400));

    @BeforeAll
    static void runContainer() {
        configServerContainer.start();
        kafka.start();
        postgresContainer.start();
        redisContainer.start();
    }

    @DynamicPropertySource
    static void dataSourcesProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgresContainer::getJdbcUrl);
        registry.add("spring.data.redis.host", redisContainer::getHost);
        registry.add("spring.data.redis.port", redisContainer::getFirstMappedPort);
        registry.add("spring.kafka.bootstrap-servers", kafka::getBootstrapServers);
    }

    @NonNull
    protected MockMultipartFile createRequest(String text) {
        return new MockMultipartFile(
                "request",
                "request",
                MediaType.APPLICATION_JSON.toString(),
                REQUEST_PATTERN.getConstant().formatted(text).getBytes()
        );
    }
}
