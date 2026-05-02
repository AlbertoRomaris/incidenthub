package com.incidenthub.core.domain.rule;

public enum RuleConditionType {
    COUNT_OVER_WINDOW,
    CONSECUTIVE_FAILURES,
    LATENCY_THRESHOLD,
    MISSING_HEARTBEAT
}