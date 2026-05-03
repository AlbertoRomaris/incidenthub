package com.incidenthub.infrastructure.persistence.alert;

import com.incidenthub.core.domain.alert.Alert;
import com.incidenthub.core.domain.alert.AlertChannel;
import com.incidenthub.core.domain.alert.AlertId;
import com.incidenthub.core.domain.alert.AlertStatus;
import com.incidenthub.core.domain.incident.IncidentId;
import jakarta.persistence.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Entity
@Table(name = "alerts")
public class AlertEntity {

    @Id
    private UUID id;

    @Column(name = "incident_id", nullable = false)
    private UUID incidentId;

    @Enumerated(EnumType.STRING)
    @Column(name = "channel", nullable = false, length = 40)
    private AlertChannel channel;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 40)
    private AlertStatus status;

    @Column(name = "title", nullable = false, columnDefinition = "TEXT")
    private String title;

    @Column(name = "message", nullable = false, columnDefinition = "TEXT")
    private String message;

    @Column(name = "created_at", nullable = false, columnDefinition = "TIMESTAMP WITH TIME ZONE")
    private Instant createdAt;

    @Column(name = "sent_at", columnDefinition = "TIMESTAMP WITH TIME ZONE")
    private Instant sentAt;

    @Column(name = "failed_at", columnDefinition = "TIMESTAMP WITH TIME ZONE")
    private Instant failedAt;

    @Column(name = "failure_reason", columnDefinition = "TEXT")
    private String failureReason;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "attributes_json", nullable = false, columnDefinition = "jsonb")
    private Map<String, Object> attributes = new HashMap<>();

    @Column(name = "updated_at", nullable = false, columnDefinition = "TIMESTAMP WITH TIME ZONE")
    private Instant updatedAt;

    protected AlertEntity() {
        // Required by JPA
    }

    private AlertEntity(
            UUID id,
            UUID incidentId,
            AlertChannel channel,
            AlertStatus status,
            String title,
            String message,
            Instant createdAt,
            Instant sentAt,
            Instant failedAt,
            String failureReason,
            Map<String, Object> attributes,
            Instant updatedAt
    ) {
        this.id = id;
        this.incidentId = incidentId;
        this.channel = channel;
        this.status = status;
        this.title = title;
        this.message = message;
        this.createdAt = createdAt;
        this.sentAt = sentAt;
        this.failedAt = failedAt;
        this.failureReason = failureReason;
        this.attributes = attributes == null ? new HashMap<>() : new HashMap<>(attributes);
        this.updatedAt = updatedAt;
    }

    public static AlertEntity fromDomain(Alert alert) {
        return new AlertEntity(
                alert.id().value(),
                alert.incidentId().value(),
                alert.channel(),
                alert.status(),
                alert.title(),
                alert.message(),
                alert.createdAt(),
                alert.sentAt(),
                alert.failedAt(),
                alert.failureReason(),
                alert.attributes(),
                Instant.now()
        );
    }

    public Alert toDomain() {
        return new Alert(
                AlertId.from(id),
                IncidentId.from(incidentId),
                channel,
                status,
                title,
                message,
                createdAt,
                sentAt,
                failedAt,
                failureReason,
                attributes
        );
    }
}