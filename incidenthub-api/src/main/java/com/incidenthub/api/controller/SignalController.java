package com.incidenthub.api.controller;

import com.incidenthub.api.dto.signal.IngestSignalRequest;
import com.incidenthub.api.dto.signal.IngestSignalResponse;
import com.incidenthub.core.application.model.IngestSignalCommand;
import com.incidenthub.core.application.model.IngestSignalResult;
import com.incidenthub.core.application.usecase.IngestSignalUseCase;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/signals")
public class SignalController {

    private final IngestSignalUseCase ingestSignalUseCase;

    public SignalController(IngestSignalUseCase ingestSignalUseCase) {
        this.ingestSignalUseCase = ingestSignalUseCase;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.ACCEPTED)
    public IngestSignalResponse ingestSignal(@Valid @RequestBody IngestSignalRequest request) {
        IngestSignalCommand command = new IngestSignalCommand(
                request.serviceName(),
                request.environment(),
                request.signalType(),
                request.severity(),
                request.message(),
                request.correlationId(),
                request.traceId(),
                request.spanId(),
                request.latencyMs(),
                request.statusCode(),
                request.errorCode(),
                request.timestamp(),
                request.attributes()
        );

        IngestSignalResult result = ingestSignalUseCase.ingest(command);

        return new IngestSignalResponse(
                result.signalId().value().toString(),
                result.status()
        );
    }
}