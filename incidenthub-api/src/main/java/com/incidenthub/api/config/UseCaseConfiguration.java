package com.incidenthub.api.config;

import com.incidenthub.core.application.port.SignalProcessingTaskRepository;
import com.incidenthub.core.application.port.SignalRepository;
import com.incidenthub.core.application.usecase.GetSignalByIdUseCase;
import com.incidenthub.core.application.usecase.IngestSignalUseCase;
import com.incidenthub.core.application.usecase.ListRecentSignalsUseCase;
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
}