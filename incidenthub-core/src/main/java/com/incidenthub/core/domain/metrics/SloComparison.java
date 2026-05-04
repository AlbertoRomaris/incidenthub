package com.incidenthub.core.domain.metrics;

public enum SloComparison {
    GREATER_THAN_OR_EQUAL,
    LESS_THAN_OR_EQUAL;

    public boolean isSatisfied(double actualValue, double targetValue) {
        return switch (this) {
            case GREATER_THAN_OR_EQUAL -> actualValue >= targetValue;
            case LESS_THAN_OR_EQUAL -> actualValue <= targetValue;
        };
    }
}
