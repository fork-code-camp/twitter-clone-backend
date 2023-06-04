package com.example.authentication.integration;

import com.example.authentication.integration.annotation.IT;
import org.junit.jupiter.api.BeforeAll;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.jdbc.Sql;
import org.testcontainers.containers.FixedHostPortGenericContainer;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.containers.wait.strategy.Wait;

@IT
@Sql({
        "classpath:sql/data.sql"
})
public class IntegrationTestBase {

    private static final PostgreSQLContainer<?> postgresContainer = new PostgreSQLContainer<>("postgres:alpine3.17");
    private static final GenericContainer<?> configServerContainer = new FixedHostPortGenericContainer<>("twitterclone0/spring-cloud-config-server")
            .withFixedExposedPort(8888, 8888)
            .waitingFor(Wait.forHttp("/authentication-service/test")
                    .forStatusCodeMatching(port -> port >= 200 && port < 400));

    @BeforeAll
    static void runContainer() {
        configServerContainer.start();
        postgresContainer.start();
    }

    @DynamicPropertySource
    static void postgresProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgresContainer::getJdbcUrl);
    }
}
