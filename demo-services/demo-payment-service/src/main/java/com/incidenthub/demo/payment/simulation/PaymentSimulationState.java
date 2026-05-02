package com.incidenthub.demo.payment.simulation;

import org.springframework.stereotype.Component;

@Component
public class PaymentSimulationState {

    private FailureMode mode = FailureMode.NORMAL;
    private int latencyMs = 0;
    private double failureRate = 0.0;

    public FailureMode mode() {
        return mode;
    }

    public int latencyMs() {
        return latencyMs;
    }

    public double failureRate() {
        return failureRate;
    }

    public void update(FailureMode mode, Integer latencyMs, Double failureRate) {
        this.mode = mode == null ? FailureMode.NORMAL : mode;
        this.latencyMs = latencyMs == null ? 0 : Math.max(0, latencyMs);
        this.failureRate = failureRate == null ? 0.0 : Math.max(0.0, Math.min(1.0, failureRate));
    }
}