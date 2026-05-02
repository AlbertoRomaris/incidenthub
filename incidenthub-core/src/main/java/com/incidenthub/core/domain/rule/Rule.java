package com.incidenthub.core.domain.rule;

import com.incidenthub.core.domain.incident.IncidentSeverity;
import com.incidenthub.core.domain.incident.IncidentType;
import com.incidenthub.core.domain.signal.SignalType;

import java.util.Objects;

public record Rule(
        RuleId id,
        String name,
        String description,
        String servicePattern,
        SignalType signalType,
        RuleConditionType conditionType,
        int threshold,
        int timeWindowSeconds,
        IncidentType incidentType,
        IncidentSeverity incidentSeverity,
        boolean enabled
) {

    public Rule {
        Objects.requireNonNull(id, "Rule id must not be null");
        name = requireText(name, "Rule name must not be blank");
        description = description == null ? "" : description.trim();
        servicePattern = requireText(servicePattern, "Service pattern must not be blank");
        Objects.requireNonNull(signalType, "Signal type must not be null");
        Objects.requireNonNull(conditionType, "Rule condition type must not be null");
        Objects.requireNonNull(incidentType, "Incident type must not be null");
        Objects.requireNonNull(incidentSeverity, "Incident severity must not be null");

        if (threshold <= 0) {
            throw new IllegalArgumentException("Rule threshold must be greater than zero");
        }

        if (timeWindowSeconds <= 0) {
            throw new IllegalArgumentException("Rule time window must be greater than zero");
        }
    }

    public static Rule enabled(
            String name,
            String description,
            String servicePattern,
            SignalType signalType,
            RuleConditionType conditionType,
            int threshold,
            int timeWindowSeconds,
            IncidentType incidentType,
            IncidentSeverity incidentSeverity
    ) {
        return new Rule(
                RuleId.newId(),
                name,
                description,
                servicePattern,
                signalType,
                conditionType,
                threshold,
                timeWindowSeconds,
                incidentType,
                incidentSeverity,
                true
        );
    }

    public boolean appliesToService(String serviceName) {
        if (serviceName == null || serviceName.isBlank()) {
            return false;
        }

        if ("*".equals(servicePattern)) {
            return true;
        }

        return servicePattern.equalsIgnoreCase(serviceName.trim());
    }

    private static String requireText(String value, String message) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(message);
        }

        return value.trim();
    }
}