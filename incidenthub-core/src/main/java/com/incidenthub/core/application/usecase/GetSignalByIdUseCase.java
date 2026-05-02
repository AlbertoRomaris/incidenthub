package com.incidenthub.core.application.usecase;

import com.incidenthub.core.application.port.SignalRepository;
import com.incidenthub.core.domain.signal.Signal;
import com.incidenthub.core.domain.signal.SignalId;

import java.util.Objects;
import java.util.Optional;

public class GetSignalByIdUseCase {

    private final SignalRepository signalRepository;

    public GetSignalByIdUseCase(SignalRepository signalRepository) {
        this.signalRepository = Objects.requireNonNull(signalRepository, "Signal repository must not be null");
    }

    public Optional<Signal> findById(SignalId signalId) {
        Objects.requireNonNull(signalId, "Signal id must not be null");

        return signalRepository.findById(signalId);
    }
}