package com.example.profile.integration;

import com.example.profile.client.StorageServiceClient;
import com.example.profile.integration.annotation.IT;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.cache.CacheManager;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.FixedHostPortGenericContainer;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.MongoDBContainer;
import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.utility.DockerImageName;

@IT
public class IntegrationTestBase {

    @Container
    private static final MongoDBContainer mongoDBContainer = new MongoDBContainer(DockerImageName.parse("mongo:jammy"));
    @Container
    @SuppressWarnings("resource")
    private static final GenericContainer<?> redisContainer = new GenericContainer<>(DockerImageName.parse("redis:7.2-rc-alpine3.18")).withExposedPorts(6379);
    @Container
    @SuppressWarnings({"unused", "deprecation", "resource"})
    private static final GenericContainer<?> configServerContainer = new FixedHostPortGenericContainer<>(
            "twitterclone0/twitter-spring-cloud-config-server:2.0.1"
    )
            .withFixedExposedPort(8888, 8888)
            .waitingFor(Wait.forHttp("/profile-service/test")
                    .forStatusCodeMatching(port -> port >= 200 && port < 400));

    @MockBean
    @SuppressWarnings("unused")
    private StorageServiceClient storageServiceClient;
    @Autowired
    protected CacheManager cacheManager;

    @BeforeEach
    @SuppressWarnings("DataFlowIssue")
    public void setUp() {
        cacheManager.getCache("profiles").clear();
        cacheManager.getCache("followers").clear();
        cacheManager.getCache("followees").clear();
        cacheManager.getCache("followees_celebrities").clear();
    }

    @DynamicPropertySource
    static void mongoProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.data.mongodb.uri", mongoDBContainer::getReplicaSetUrl);
        registry.add("spring.data.redis.host", redisContainer::getHost);
        registry.add("spring.data.redis.port", redisContainer::getFirstMappedPort);
    }
}
