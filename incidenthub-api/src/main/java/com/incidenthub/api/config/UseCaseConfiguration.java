package com.incidenthub.api.config;

import com.incidenthub.core.application.port.SignalRepository;
import com.incidenthub.core.application.usecase.IngestSignalUseCase;
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
            Clock clock
    ) {
        return new IngestSignalUseCase(signalRepository, clock);
    }
}