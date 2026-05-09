package com.incidenthub.worker.config;

import com.incidenthub.core.application.port.IncidentEvidenceRepository;
import com.incidenthub.core.application.port.IncidentRepository;
import com.incidenthub.core.application.port.RuleRepository;
import com.incidenthub.core.application.port.SignalRepository;
import com.incidenthub.core.application.usecase.ProcessSignalUseCase;
import com.incidenthub.core.application.port.IncidentTimelineRepository;
import com.incidenthub.core.application.port.AlertRepository;
import com.incidenthub.core.application.port.AlertSender;
import com.incidenthub.core.application.usecase.SendPendingAlertsUseCase;
import com.incidenthub.core.domain.alert.AlertChannel;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Clock;

@Configuration
public class WorkerUseCaseConfiguration {

    @Bean
    public Clock clock() {
        return Clock.systemUTC();
    }

    @Bean
    public ProcessSignalUseCase processSignalUseCase(
            SignalRepository signalRepository,
            RuleRepository ruleRepository,
            IncidentRepository incidentRepository,
            IncidentEvidenceRepository incidentEvidenceRepository,
            IncidentTimelineRepository incidentTimelineRepository,
            AlertRepository alertRepository,
            Clock clock,
            @Value("${incidenthub.alerts.channel:LOG}") AlertChannel alertChannel
    ) {
        return new ProcessSignalUseCase(
                signalRepository,
                ruleRepository,
                incidentRepository,
                incidentEvidenceRepository,
                incidentTimelineRepository,
                alertRepository,
                clock,
                alertChannel
        );
    }

    @Bean
    public SendPendingAlertsUseCase sendPendingAlertsUseCase(
            AlertRepository alertRepository,
            AlertSender alertSender,
            IncidentTimelineRepository incidentTimelineRepository,
            Clock clock
    ) {
        return new SendPendingAlertsUseCase(
                alertRepository,
                alertSender,
                incidentTimelineRepository,
                clock
        );
    }
}