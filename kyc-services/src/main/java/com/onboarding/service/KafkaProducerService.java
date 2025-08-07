package com.onboarding.service;

import com.onboarding.config.KafkaTopicConfig;
import com.onboarding.dto.KycStatusUpdateEvent;
import com.onboarding.dto.NewKycApplicationEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
public class KafkaProducerService {
    private static final Logger LOGGER = LoggerFactory.getLogger(KafkaProducerService.class);
    private final KafkaTemplate<String, Object> kafkaTemplate;

    public KafkaProducerService(KafkaTemplate<String, Object> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    public void sendNewApplicationNotification(NewKycApplicationEvent event) {
        LOGGER.info("Publishing new KYC application event for ID: {}", event.getApplicationId());
        kafkaTemplate.send(KafkaTopicConfig.TOPIC_KYC_APPLICATION_SUBMITTED, event);
    }

    public void sendKycUpdateNotification(KycStatusUpdateEvent event) {
        LOGGER.info("Publishing KYC status update for customer: {}", event.getCustomerName());
        kafkaTemplate.send(KafkaTopicConfig.TOPIC_KYC_STATUS_UPDATE, event);
    }
}