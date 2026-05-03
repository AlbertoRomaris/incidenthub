package com.incidenthub.api.dto.rule;

import com.incidenthub.core.domain.incident.IncidentSeverity;
import com.incidenthub.core.domain.incident.IncidentType;
import com.incidenthub.core.domain.rule.RuleConditionType;
import com.incidenthub.core.domain.signal.SignalType;

public record RuleResponse(
        String ruleId,
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
}