package com.incidenthub.demo.payment.dto;

import com.incidenthub.demo.payment.simulation.FailureMode;

public record UpdateFailureModeRequest(
        FailureMode mode,
        Integer latencyMs,
        Double failureRate
) {
}