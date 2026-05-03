package com.incidenthub.infrastructure.persistence.timeline;

import com.incidenthub.core.domain.incident.IncidentId;
import com.incidenthub.core.domain.timeline.IncidentTimelineEvent;
import com.incidenthub.core.domain.timeline.IncidentTimelineEventId;
import com.incidenthub.core.domain.timeline.IncidentTimelineEventType;
import jakarta.persistence.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Entity
@Table(name = "incident_timeline_events")
public class IncidentTimelineEventEntity {

    @Id
    private UUID id;

    @Column(name = "incident_id", nullable = false)
    private UUID incidentId;

    @Enumerated(EnumType.STRING)
    @Column(name = "event_type", nullable = false, length = 80)
    private IncidentTimelineEventType type;

    @Column(name = "occurred_at", nullable = false, columnDefinition = "TIMESTAMP WITH TIME ZONE")
    private Instant occurredAt;

    @Column(name = "summary", nullable = false, columnDefinition = "TEXT")
    private String summary;

    @Column(name = "actor", nullable = false, length = 120)
    private String actor;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "attributes_json", nullable = false, columnDefinition = "jsonb")
    private Map<String, Object> attributes = new HashMap<>();

    @Column(name = "created_at", nullable = false, columnDefinition = "TIMESTAMP WITH TIME ZONE")
    private Instant createdAt;

    protected IncidentTimelineEventEntity() {
        // Required by JPA
    }

    private IncidentTimelineEventEntity(
            UUID id,
            UUID incidentId,
            IncidentTimelineEventType type,
            Instant occurredAt,
            String summary,
            String actor,
            Map<String, Object> attributes,
            Instant createdAt
    ) {
        this.id = id;
        this.incidentId = incidentId;
        this.type = type;
        this.occurredAt = occurredAt;
        this.summary = summary;
        this.actor = actor;
        this.attributes = attributes == null ? new HashMap<>() : new HashMap<>(attributes);
        this.createdAt = createdAt;
    }

    public static IncidentTimelineEventEntity fromDomain(IncidentTimelineEvent event) {
        return new IncidentTimelineEventEntity(
                event.id().value(),
                event.incidentId().value(),
                event.type(),
                event.occurredAt(),
                event.summary(),
                event.actor(),
                event.attributes(),
                Instant.now()
        );
    }

    public IncidentTimelineEvent toDomain() {
        return new IncidentTimelineEvent(
                IncidentTimelineEventId.from(id),
                IncidentId.from(incidentId),
                type,
                occurredAt,
                summary,
                actor,
                attributes
        );
    }
}