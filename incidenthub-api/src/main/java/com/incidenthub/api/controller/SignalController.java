package com.incidenthub.api.controller;

import com.incidenthub.api.dto.signal.IngestSignalRequest;
import com.incidenthub.api.dto.signal.IngestSignalResponse;
import com.incidenthub.api.dto.signal.SignalResponse;
import com.incidenthub.core.application.model.IngestSignalCommand;
import com.incidenthub.core.application.model.IngestSignalResult;
import com.incidenthub.core.application.usecase.GetSignalByIdUseCase;
import com.incidenthub.core.application.usecase.IngestSignalUseCase;
import com.incidenthub.core.application.usecase.ListRecentSignalsUseCase;
import com.incidenthub.core.domain.signal.Signal;
import com.incidenthub.core.domain.signal.SignalId;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/signals")
public class SignalController {

    private final IngestSignalUseCase ingestSignalUseCase;
    private final GetSignalByIdUseCase getSignalByIdUseCase;
    private final ListRecentSignalsUseCase listRecentSignalsUseCase;

    public SignalController(
            IngestSignalUseCase ingestSignalUseCase,
            GetSignalByIdUseCase getSignalByIdUseCase,
            ListRecentSignalsUseCase listRecentSignalsUseCase
    ) {
        this.ingestSignalUseCase = ingestSignalUseCase;
        this.getSignalByIdUseCase = getSignalByIdUseCase;
        this.listRecentSignalsUseCase = listRecentSignalsUseCase;
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

    @GetMapping("/{signalId}")
    public SignalResponse getSignalById(@PathVariable("signalId") UUID signalId) {
        Signal signal = getSignalByIdUseCase.findById(SignalId.from(signalId))
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Signal not found"));

        return toResponse(signal);
    }

    @GetMapping
    public List<SignalResponse> listRecentSignals(
            @RequestParam(name = "limit", required = false) Integer limit
    ) {
        return listRecentSignalsUseCase.listRecent(limit)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    private SignalResponse toResponse(Signal signal) {
        return new SignalResponse(
                signal.id().value().toString(),
                signal.serviceName(),
                signal.environment(),
                signal.type(),
                signal.severity(),
                signal.message(),
                signal.correlationId(),
                signal.traceId(),
                signal.spanId(),
                signal.latencyMs(),
                signal.statusCode(),
                signal.errorCode(),
                signal.observedAt(),
                signal.receivedAt(),
                signal.attributes()
        );
    }
}