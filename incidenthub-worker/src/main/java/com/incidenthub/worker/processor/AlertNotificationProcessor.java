package com.incidenthub.worker.processor;

import com.incidenthub.core.application.model.SendPendingAlertsResult;
import com.incidenthub.core.application.usecase.SendPendingAlertsUseCase;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class AlertNotificationProcessor {

    private static final Logger log = LoggerFactory.getLogger(AlertNotificationProcessor.class);

    private final SendPendingAlertsUseCase sendPendingAlertsUseCase;
    private final int batchSize;

    public AlertNotificationProcessor(
            SendPendingAlertsUseCase sendPendingAlertsUseCase,
            @Value("${incidenthub.alerts.batch-size:10}") int batchSize
    ) {
        this.sendPendingAlertsUseCase = sendPendingAlertsUseCase;
        this.batchSize = batchSize;
    }

    @Scheduled(fixedDelayString = "${incidenthub.alerts.poll-delay-ms:5000}")
    public void sendPendingAlerts() {
        SendPendingAlertsResult result = sendPendingAlertsUseCase.sendPending(batchSize);

        if (result.processedAlerts() == 0) {
            log.debug("No pending alerts found");
            return;
        }

        log.info(
                "Processed pending alerts: processed={}, sent={}, failed={}",
                result.processedAlerts(),
                result.sentAlerts(),
                result.failedAlerts()
        );
    }
}