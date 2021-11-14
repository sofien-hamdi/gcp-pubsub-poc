package com.kt.pocs.gcp.pubsub;

import com.google.api.gax.core.CredentialsProvider;
import com.google.api.gax.core.NoCredentialsProvider;
import com.google.api.gax.grpc.GrpcTransportChannel;
import com.google.api.gax.rpc.AlreadyExistsException;
import com.google.api.gax.rpc.FixedTransportChannelProvider;
import com.google.api.gax.rpc.TransportChannelProvider;
import com.google.cloud.pubsub.v1.SubscriptionAdminClient;
import com.google.cloud.pubsub.v1.SubscriptionAdminSettings;
import com.google.cloud.pubsub.v1.TopicAdminClient;
import com.google.cloud.pubsub.v1.TopicAdminSettings;
import com.google.pubsub.v1.ProjectSubscriptionName;
import com.google.pubsub.v1.ProjectTopicName;
import com.google.pubsub.v1.PushConfig;
import com.google.pubsub.v1.Subscription;
import com.google.pubsub.v1.Topic;
import io.grpc.ManagedChannel;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;

@Slf4j
public class PubSubResourceCreator {

    private final TransportChannelProvider channelProvider;
    private final CredentialsProvider credentialsProvider;
    private final TopicAdminClient topicAdminClient;
    private final SubscriptionAdminClient subscriptionAdminClient;
    private final String projectId;

    public PubSubResourceCreator(ManagedChannel channel, String projectId) throws IOException {
        this.projectId = projectId;
        channelProvider = FixedTransportChannelProvider.create(GrpcTransportChannel.create(channel));
        //TODO No credentials managed
        credentialsProvider = NoCredentialsProvider.create();
        topicAdminClient = topicAdminClient();
        subscriptionAdminClient = subscriptionAdminClient();
    }

    public Subscription createSubscription(String topicName, String subscriptionName) {
        ProjectTopicName topic = ProjectTopicName.of(projectId, topicName);
        ProjectSubscriptionName subscription = ProjectSubscriptionName.of(projectId, subscriptionName);

        try {
            log.info("Creating subscription: {}", subscription);
            return subscriptionAdminClient
                    .createSubscription(subscription, topic, PushConfig.getDefaultInstance(), 100);
        } catch (AlreadyExistsException e) {
            return subscriptionAdminClient.getSubscription(subscription);
        }
    }

    public Topic createTopic(String topicName) {
        ProjectTopicName topic = ProjectTopicName.of(projectId, topicName);
        try {
            log.info("Creating topic: {}", topic);
            return topicAdminClient.createTopic(topic);
        } catch (AlreadyExistsException e) {
            return topicAdminClient.getTopic(topic);
        }
    }

    private TopicAdminClient topicAdminClient() throws IOException {
        return TopicAdminClient.create(
                TopicAdminSettings.newBuilder()
                        .setTransportChannelProvider(channelProvider)
                        .setCredentialsProvider(credentialsProvider).build());
    }

    private SubscriptionAdminClient subscriptionAdminClient() throws IOException {
        return SubscriptionAdminClient.create(SubscriptionAdminSettings.newBuilder()
                .setTransportChannelProvider(channelProvider)
                .setCredentialsProvider(credentialsProvider)
                .build());
    }

}