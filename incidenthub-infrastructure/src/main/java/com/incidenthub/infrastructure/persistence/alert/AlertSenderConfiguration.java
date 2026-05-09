package com.incidenthub.infrastructure.persistence.alert;

import com.incidenthub.core.application.port.AlertSender;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.services.sns.SnsClient;

@Configuration
public class AlertSenderConfiguration {

    @Bean
    @ConditionalOnProperty(
            name = "incidenthub.alerts.sender",
            havingValue = "log",
            matchIfMissing = true
    )
    public AlertSender logAlertSender() {
        return new LogAlertSender();
    }

    @Bean
    @ConditionalOnProperty(
            name = "incidenthub.alerts.sender",
            havingValue = "sns"
    )
    public SnsClient snsClient() {
        return SnsClient.create();
    }

    @Bean
    @ConditionalOnProperty(
            name = "incidenthub.alerts.sender",
            havingValue = "sns"
    )
    public AlertSender snsAlertSender(
            SnsClient snsClient,
            org.springframework.core.env.Environment environment
    ) {
        String topicArn = environment.getProperty("incidenthub.alerts.sns.topic-arn");

        if (topicArn == null || topicArn.isBlank()) {
            throw new IllegalStateException("incidenthub.alerts.sns.topic-arn is required when incidenthub.alerts.sender=sns");
        }

        return new SnsAlertSender(snsClient, topicArn);
    }
}