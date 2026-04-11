package com.temkarstudios.parkinglot;

import jakarta.annotation.PreDestroy;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.junit.jupiter.Testcontainers;

@TestConfiguration
@Testcontainers
public class EmbeddedRedisTestConfig {
    private static GenericContainer<?> redisContainer;

    @DynamicPropertySource
    static void redisProperties(DynamicPropertyRegistry registry) {
        redisContainer = new GenericContainer<>("redis:7.0.12-alpine")
                .withExposedPorts(6379)
                .waitingFor(Wait.forListeningPort());
        redisContainer.start();
        registry.add("spring.redis.host", () -> redisContainer.getHost());
        registry.add("spring.redis.port", () -> redisContainer.getMappedPort(6379));
    }

    @PreDestroy
    void stop() {
        if (redisContainer != null) {
            redisContainer.stop();
        }
    }
}
