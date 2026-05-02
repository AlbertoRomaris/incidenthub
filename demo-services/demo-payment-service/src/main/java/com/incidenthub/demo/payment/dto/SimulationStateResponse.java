package com.incidenthub.demo.payment.dto;

import com.incidenthub.demo.payment.simulation.FailureMode;

public record SimulationStateResponse(
        FailureMode mode,
        int latencyMs,
        double failureRate
) {
}