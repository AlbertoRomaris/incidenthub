package com.incidenthub.api.config;

import com.incidenthub.core.application.port.SignalProcessingTaskRepository;
import com.incidenthub.core.application.port.SignalRepository;
import com.incidenthub.core.application.usecase.GetSignalByIdUseCase;
import com.incidenthub.core.application.usecase.IngestSignalUseCase;
import com.incidenthub.core.application.usecase.ListRecentSignalsUseCase;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import com.incidenthub.core.application.port.IncidentEvidenceRepository;
import com.incidenthub.core.application.port.IncidentRepository;
import com.incidenthub.core.application.usecase.GetIncidentByIdUseCase;
import com.incidenthub.core.application.usecase.ListIncidentEvidenceUseCase;
import com.incidenthub.core.application.usecase.ListOpenIncidentsUseCase;
import com.incidenthub.core.application.usecase.AcknowledgeIncidentUseCase;
import com.incidenthub.core.application.usecase.ResolveIncidentUseCase;

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
    public ListOpenIncidentsUseCase listOpenIncidentsUseCase(IncidentRepository incidentRepository) {
        return new ListOpenIncidentsUseCase(incidentRepository);
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
            Clock clock
    ) {
        return new AcknowledgeIncidentUseCase(incidentRepository, clock);
    }

    @Bean
    public ResolveIncidentUseCase resolveIncidentUseCase(
            IncidentRepository incidentRepository,
            Clock clock
    ) {
        return new ResolveIncidentUseCase(incidentRepository, clock);
    }
}