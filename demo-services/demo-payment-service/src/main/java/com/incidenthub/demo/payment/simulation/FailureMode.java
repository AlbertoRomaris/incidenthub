package com.incidenthub.demo.payment.simulation;

public enum FailureMode {
    NORMAL,
    RANDOM_FAILURES,
    HIGH_LATENCY,
    TIMEOUTS,
    DOWN
}