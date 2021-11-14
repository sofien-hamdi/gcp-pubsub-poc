package com.kt.pocs.gcp.pubsub;

import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.MapPropertySource;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.wait.strategy.LogMessageWaitStrategy;

import java.io.IOException;
import java.time.Duration;
import java.time.Instant;
import java.util.LinkedHashMap;

import static java.lang.String.format;


@Slf4j
@Configuration
@EnableConfigurationProperties(PubsubProperties.class)
public class PubsubTestConfiguration {

    @Bean(name = "pubsubContainer", destroyMethod = "stop")
    public GenericContainer pubsub(ConfigurableEnvironment environment, PubsubProperties properties) {
        log.info("Starting Google Cloud Pubsub emulator");

        GenericContainer container = new GenericContainer(properties.getDockerImage())
                .withExposedPorts(properties.getPort())
                .withCommand(
                        "/bin/sh",
                        "-c",
                        format(
                                "gcloud beta emulators pubsub start --project %s --host-port=%s:%d ",
                                properties.getProjectId(),
                                properties.getHost(),
                                properties.getPort()
                        )
                )
                .waitingFor(new LogMessageWaitStrategy().withRegEx("(?s).*started.*$"))
                .withStartupTimeout(properties.getTimeoutDuration())
                .withReuse(properties.isReuseContainer());

        Instant startTime = Instant.now();
        container.start();
        Instant endTime = Instant.now();

        long startupTime = Duration.between(startTime, endTime).toMillis() / 1000;

        log.info("Container started in : {} s", startupTime);

        registerPubsubEnvironment(container, environment, properties);

        return container;
    }

    private void registerPubsubEnvironment(GenericContainer container, ConfigurableEnvironment environment, PubsubProperties properties) {
        LinkedHashMap<String, Object> map = new LinkedHashMap<>();
        map.put("gcp.pubsub.port", container.getMappedPort(properties.getPort()));
        map.put("gcp.pubsub.host", container.getContainerIpAddress());
        map.put("gcp.pubsub.project-id", properties.getProjectId());

        MapPropertySource propertySource = new MapPropertySource("pubsubInfo", map);
        environment.getPropertySources().addFirst(propertySource);
    }

    @Bean(name = "managedChannel")
    public ManagedChannel managedChannel(@Qualifier("pubsubContainer") GenericContainer pubsub, PubsubProperties properties) {
        return ManagedChannelBuilder.forAddress(pubsub.getContainerIpAddress(),
                pubsub.getMappedPort(properties.getPort()))
                .usePlaintext()
                .build();
    }

    @Bean(name = "pubsubResourcesGenerator")
    public PubSubResourceCreator pubSubResourcesGenerator(@Qualifier("managedChannel") ManagedChannel managedChannel, PubsubProperties properties) throws IOException {
        return new PubSubResourceCreator(managedChannel, properties.getProjectId());
    }
}