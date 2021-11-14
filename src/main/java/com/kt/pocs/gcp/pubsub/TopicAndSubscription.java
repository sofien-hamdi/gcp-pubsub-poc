package com.kt.pocs.gcp.pubsub;

import lombok.Data;

@Data
public class TopicAndSubscription {
    private String topic;
    private String subscription;
}