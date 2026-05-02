package com.incidenthub.infrastructure.persistence.evidence;

import com.incidenthub.core.domain.evidence.IncidentEvidence;
import com.incidenthub.core.domain.evidence.IncidentEvidenceId;
import com.incidenthub.core.domain.incident.IncidentId;
import com.incidenthub.core.domain.rule.RuleId;
import com.incidenthub.core.domain.signal.SignalId;
import jakarta.persistence.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Entity
@Table(name = "incident_evidence")
public class IncidentEvidenceEntity {

    @Id
    private UUID id;

    @Column(name = "incident_id", nullable = false)
    private UUID incidentId;

    @Column(name = "signal_id", nullable = false)
    private UUID signalId;

    @Column(name = "rule_id", nullable = false)
    private UUID ruleId;

    @Column(name = "captured_at", nullable = false, columnDefinition = "TIMESTAMP WITH TIME ZONE")
    private Instant capturedAt;

    @Column(name = "summary", nullable = false, columnDefinition = "TEXT")
    private String summary;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "attributes_json", nullable = false, columnDefinition = "jsonb")
    private Map<String, Object> attributes = new HashMap<>();

    @Column(name = "created_at", nullable = false, columnDefinition = "TIMESTAMP WITH TIME ZONE")
    private Instant createdAt;

    protected IncidentEvidenceEntity() {
        // Required by JPA
    }

    private IncidentEvidenceEntity(
            UUID id,
            UUID incidentId,
            UUID signalId,
            UUID ruleId,
            Instant capturedAt,
            String summary,
            Map<String, Object> attributes,
            Instant createdAt
    ) {
        this.id = id;
        this.incidentId = incidentId;
        this.signalId = signalId;
        this.ruleId = ruleId;
        this.capturedAt = capturedAt;
        this.summary = summary;
        this.attributes = attributes == null ? new HashMap<>() : new HashMap<>(attributes);
        this.createdAt = createdAt;
    }

    public static IncidentEvidenceEntity fromDomain(IncidentEvidence evidence) {
        return new IncidentEvidenceEntity(
                evidence.id().value(),
                evidence.incidentId().value(),
                evidence.signalId().value(),
                evidence.ruleId().value(),
                evidence.capturedAt(),
                evidence.summary(),
                evidence.attributes(),
                Instant.now()
        );
    }

    public IncidentEvidence toDomain() {
        return new IncidentEvidence(
                IncidentEvidenceId.from(id),
                IncidentId.from(incidentId),
                SignalId.from(signalId),
                RuleId.from(ruleId),
                capturedAt,
                summary,
                attributes
        );
    }
}