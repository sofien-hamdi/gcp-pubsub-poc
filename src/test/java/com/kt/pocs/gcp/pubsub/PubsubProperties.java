package com.kt.pocs.gcp.pubsub;

import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.time.Duration;


@Data
@EqualsAndHashCode
@ConfigurationProperties("gcp.pubsub")
public class PubsubProperties {

    private String dockerImage = "google/cloud-sdk:337.0.0";

    private String host;
    private int port;
    private String projectId;

    private long waitTimeoutInSeconds;
    private boolean reuseContainer = false;

    public Duration getTimeoutDuration() {
        return Duration.ofSeconds(waitTimeoutInSeconds);
    }
}
