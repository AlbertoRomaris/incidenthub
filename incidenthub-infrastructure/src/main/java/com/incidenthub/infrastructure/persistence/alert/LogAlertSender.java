package com.incidenthub.infrastructure.alert;

import com.incidenthub.core.application.port.AlertSender;
import com.incidenthub.core.domain.alert.Alert;
import com.incidenthub.core.domain.alert.AlertChannel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class LogAlertSender implements AlertSender {

    private static final Logger log = LoggerFactory.getLogger(LogAlertSender.class);

    @Override
    public void send(Alert alert) {
        if (alert.channel() != AlertChannel.LOG) {
            throw new IllegalArgumentException("Unsupported alert channel: " + alert.channel());
        }

        log.warn(
                "INCIDENT ALERT | alertId={} | incidentId={} | title={} | message={} | attributes={}",
                alert.id().value(),
                alert.incidentId().value(),
                alert.title(),
                alert.message(),
                alert.attributes()
        );
    }
}