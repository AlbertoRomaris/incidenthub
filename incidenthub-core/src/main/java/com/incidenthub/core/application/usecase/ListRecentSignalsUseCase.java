package com.incidenthub.core.application.usecase;

import com.incidenthub.core.application.port.SignalRepository;
import com.incidenthub.core.domain.signal.Signal;

import java.util.List;
import java.util.Objects;

public class ListRecentSignalsUseCase {

    private static final int DEFAULT_LIMIT = 50;
    private static final int MAX_LIMIT = 200;

    private final SignalRepository signalRepository;

    public ListRecentSignalsUseCase(SignalRepository signalRepository) {
        this.signalRepository = Objects.requireNonNull(signalRepository, "Signal repository must not be null");
    }

    public List<Signal> listRecent(Integer requestedLimit) {
        int limit = normalizeLimit(requestedLimit);

        return signalRepository.findRecent(limit);
    }

    private int normalizeLimit(Integer requestedLimit) {
        if (requestedLimit == null) {
            return DEFAULT_LIMIT;
        }

        if (requestedLimit <= 0) {
            return DEFAULT_LIMIT;
        }

        return Math.min(requestedLimit, MAX_LIMIT);
    }
}