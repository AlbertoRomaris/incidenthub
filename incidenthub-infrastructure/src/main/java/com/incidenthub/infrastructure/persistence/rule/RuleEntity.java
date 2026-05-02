package com.incidenthub.infrastructure.persistence.rule;

import com.incidenthub.core.domain.incident.IncidentSeverity;
import com.incidenthub.core.domain.incident.IncidentType;
import com.incidenthub.core.domain.rule.Rule;
import com.incidenthub.core.domain.rule.RuleConditionType;
import com.incidenthub.core.domain.rule.RuleId;
import com.incidenthub.core.domain.signal.SignalType;
import jakarta.persistence.*;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "rules")
public class RuleEntity {

    @Id
    private UUID id;

    @Column(name = "name", nullable = false, length = 160)
    private String name;

    @Column(name = "description", nullable = false, columnDefinition = "TEXT")
    private String description;

    @Column(name = "service_pattern", nullable = false, length = 120)
    private String servicePattern;

    @Enumerated(EnumType.STRING)
    @Column(name = "signal_type", nullable = false, length = 60)
    private SignalType signalType;

    @Enumerated(EnumType.STRING)
    @Column(name = "condition_type", nullable = false, length = 80)
    private RuleConditionType conditionType;

    @Column(name = "threshold", nullable = false)
    private int threshold;

    @Column(name = "time_window_seconds", nullable = false)
    private int timeWindowSeconds;

    @Enumerated(EnumType.STRING)
    @Column(name = "incident_type", nullable = false, length = 80)
    private IncidentType incidentType;

    @Enumerated(EnumType.STRING)
    @Column(name = "incident_severity", nullable = false, length = 40)
    private IncidentSeverity incidentSeverity;

    @Column(name = "enabled", nullable = false)
    private boolean enabled;

    @Column(name = "created_at", nullable = false, columnDefinition = "TIMESTAMP WITH TIME ZONE")
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false, columnDefinition = "TIMESTAMP WITH TIME ZONE")
    private Instant updatedAt;

    protected RuleEntity() {
        // Required by JPA
    }

    public Rule toDomain() {
        return new Rule(
                RuleId.from(id),
                name,
                description,
                servicePattern,
                signalType,
                conditionType,
                threshold,
                timeWindowSeconds,
                incidentType,
                incidentSeverity,
                enabled
        );
    }
}