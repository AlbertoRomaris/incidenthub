package com.incidenthub.core.application.usecase;

import com.incidenthub.core.application.model.IngestSignalCommand;
import com.incidenthub.core.application.model.IngestSignalResult;
import com.incidenthub.core.application.port.SignalProcessingTaskRepository;
import com.incidenthub.core.application.port.SignalRepository;
import com.incidenthub.core.domain.signal.Signal;

import java.time.Clock;
import java.time.Instant;
import java.util.Objects;

public class IngestSignalUseCase {

    private final SignalRepository signalRepository;
    private final SignalProcessingTaskRepository signalProcessingTaskRepository;
    private final Clock clock;

    public IngestSignalUseCase(
            SignalRepository signalRepository,
            SignalProcessingTaskRepository signalProcessingTaskRepository,
            Clock clock
    ) {
        this.signalRepository = Objects.requireNonNull(signalRepository, "Signal repository must not be null");
        this.signalProcessingTaskRepository = Objects.requireNonNull(signalProcessingTaskRepository, "Signal processing task repository must not be null");
        this.clock = Objects.requireNonNull(clock, "Clock must not be null");
    }

    public IngestSignalResult ingest(IngestSignalCommand command) {
        Objects.requireNonNull(command, "Ingest signal command must not be null");

        Instant receivedAt = Instant.now(clock);
        Instant observedAt = command.observedAt() != null
                ? command.observedAt()
                : receivedAt;

        Signal signal = Signal.received(
                command.serviceName(),
                command.environment(),
                command.type(),
                command.severity(),
                command.message(),
                command.correlationId(),
                command.traceId(),
                command.spanId(),
                command.latencyMs(),
                command.statusCode(),
                command.errorCode(),
                observedAt,
                receivedAt,
                command.attributes()
        );

        Signal savedSignal = signalRepository.save(signal);

        signalProcessingTaskRepository.createPending(
                savedSignal.id(),
                receivedAt
        );

        return IngestSignalResult.accepted(savedSignal.id());
    }
}