package com.incidenthub.infrastructure.persistence.alert;

import com.incidenthub.core.application.port.AlertSender;
import com.incidenthub.core.domain.alert.Alert;
import com.incidenthub.core.domain.alert.AlertChannel;
import software.amazon.awssdk.services.sns.SnsClient;
import software.amazon.awssdk.services.sns.model.PublishRequest;

public class SnsAlertSender implements AlertSender {

    private final SnsClient snsClient;
    private final String topicArn;

    public SnsAlertSender(SnsClient snsClient, String topicArn) {
        this.snsClient = snsClient;
        this.topicArn = topicArn;
    }

    @Override
    public void send(Alert alert) {
        if (alert.channel() != AlertChannel.SNS) {
            throw new IllegalArgumentException("Unsupported alert channel: " + alert.channel());
        }

        PublishRequest request = PublishRequest.builder()
                .topicArn(topicArn)
                .subject(alert.title())
                .message(buildMessage(alert))
                .build();

        snsClient.publish(request);
    }

    private String buildMessage(Alert alert) {
        return """
                IncidentHub Alert

                Alert ID: %s
                Incident ID: %s
                Title: %s

                Message:
                %s

                Attributes:
                %s
                """.formatted(
                alert.id().value(),
                alert.incidentId().value(),
                alert.title(),
                alert.message(),
                alert.attributes()
        );
    }
}