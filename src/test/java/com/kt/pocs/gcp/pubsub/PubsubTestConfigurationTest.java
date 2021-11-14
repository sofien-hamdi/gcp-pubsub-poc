package com.kt.pocs.gcp.pubsub;

import static org.assertj.core.api.Assertions.assertThat;

import com.google.cloud.spring.pubsub.core.PubSubTemplate;
import com.google.cloud.spring.pubsub.support.AcknowledgeablePubsubMessage;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;

@Slf4j
@SpringBootTest(classes = PubSubFakeApplication.class)
@ActiveProfiles("test")
public class PubsubTestConfigurationTest {

    @Autowired
    private PubSubTemplate pubSubTemplate;

    @Autowired
    private PubSubResourceCreator pubSubResourceCreator;

    @BeforeEach
    public void setUp() {
        pubSubResourceCreator.createTopic("topic0");
        pubSubResourceCreator.createSubscription("topic0", "subscription0");
    }

    @Test
    public void shouldPublishAndConsumeMessage() {

        pubSubTemplate.publish("topic0", "Hi, I am here !");

        List<AcknowledgeablePubsubMessage> messages = pubSubTemplate.pull("subscription0", 1, false);

        assertThat(messages.size()).isEqualTo(1);
    }
}