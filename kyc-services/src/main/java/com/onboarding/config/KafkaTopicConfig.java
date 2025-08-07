package com.onboarding.config;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;

@Configuration
public class KafkaTopicConfig {

    public static final String TOPIC_KYC_APPLICATION_SUBMITTED = "kyc.application.submitted";
    public static final String TOPIC_KYC_STATUS_UPDATE = "kyc.status.update";

    @Bean
    public NewTopic kycApplicationSubmittedTopic() {
        return TopicBuilder.name(TOPIC_KYC_APPLICATION_SUBMITTED)
                .partitions(1)
                .replicas(1)
                .build();
    }

    @Bean
    public NewTopic kycStatusUpdateTopic() {
        return TopicBuilder.name(TOPIC_KYC_STATUS_UPDATE)
                .partitions(1)
                .replicas(1)
                .build();
    }
}