package com.incidenthub.core.application.port;

import com.incidenthub.core.domain.signal.Signal;
import com.incidenthub.core.domain.signal.SignalId;

import java.util.Optional;

public interface SignalRepository {

    Signal save(Signal signal);

    Optional<Signal> findById(SignalId signalId);
}