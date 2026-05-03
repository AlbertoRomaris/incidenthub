package com.incidenthub.api.config;

import com.incidenthub.core.application.port.*;
import com.incidenthub.core.application.usecase.*;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import java.time.Clock;

@Configuration
public class UseCaseConfiguration {

    @Bean
    public Clock clock() {
        return Clock.systemUTC();
    }

    @Bean
    public IngestSignalUseCase ingestSignalUseCase(
            SignalRepository signalRepository,
            SignalProcessingTaskRepository signalProcessingTaskRepository,
            Clock clock
    ) {
        return new IngestSignalUseCase(
                signalRepository,
                signalProcessingTaskRepository,
                clock
        );
    }

    @Bean
    public GetSignalByIdUseCase getSignalByIdUseCase(SignalRepository signalRepository) {
        return new GetSignalByIdUseCase(signalRepository);
    }

    @Bean
    public ListRecentSignalsUseCase listRecentSignalsUseCase(SignalRepository signalRepository) {
        return new ListRecentSignalsUseCase(signalRepository);
    }

    @Bean
    public GetIncidentByIdUseCase getIncidentByIdUseCase(IncidentRepository incidentRepository) {
        return new GetIncidentByIdUseCase(incidentRepository);
    }

    @Bean
    public ListIncidentsUseCase listIncidentsUseCase(IncidentRepository incidentRepository) {
        return new ListIncidentsUseCase(incidentRepository);
    }

    @Bean
    public ListIncidentEvidenceUseCase listIncidentEvidenceUseCase(
            IncidentEvidenceRepository incidentEvidenceRepository
    ) {
        return new ListIncidentEvidenceUseCase(incidentEvidenceRepository);
    }

    @Bean
    public AcknowledgeIncidentUseCase acknowledgeIncidentUseCase(
            IncidentRepository incidentRepository,
            IncidentTimelineRepository incidentTimelineRepository,
            Clock clock
    ) {
        return new AcknowledgeIncidentUseCase(
                incidentRepository,
                incidentTimelineRepository,
                clock
        );
    }

    @Bean
    public ResolveIncidentUseCase resolveIncidentUseCase(
            IncidentRepository incidentRepository,
            IncidentTimelineRepository incidentTimelineRepository,
            Clock clock
    ) {
        return new ResolveIncidentUseCase(
                incidentRepository,
                incidentTimelineRepository,
                clock
        );
    }

    @Bean
    public ListEnabledRulesUseCase listEnabledRulesUseCase(RuleRepository ruleRepository) {
        return new ListEnabledRulesUseCase(ruleRepository);
    }

    @Bean
    public GetRuleByIdUseCase getRuleByIdUseCase(RuleRepository ruleRepository) {
        return new GetRuleByIdUseCase(ruleRepository);
    }

    @Bean
    public ListIncidentTimelineUseCase listIncidentTimelineUseCase(
            IncidentTimelineRepository incidentTimelineRepository
    ) {
        return new ListIncidentTimelineUseCase(incidentTimelineRepository);
    }
}