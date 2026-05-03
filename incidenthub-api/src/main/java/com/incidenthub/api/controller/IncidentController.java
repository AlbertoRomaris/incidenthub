package com.incidenthub.api.controller;

import com.incidenthub.api.dto.incident.IncidentEvidenceResponse;
import com.incidenthub.api.dto.incident.IncidentResponse;
import com.incidenthub.api.dto.incident.IncidentTimelineEventResponse;
import com.incidenthub.core.application.usecase.*;
import com.incidenthub.core.domain.incident.IncidentStatus;
import com.incidenthub.core.domain.evidence.IncidentEvidence;
import com.incidenthub.core.domain.incident.Incident;
import com.incidenthub.core.domain.incident.IncidentId;
import com.incidenthub.core.domain.timeline.IncidentTimelineEvent;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import com.incidenthub.api.dto.incident.IncidentAlertResponse;
import com.incidenthub.core.application.usecase.ListIncidentAlertsUseCase;
import com.incidenthub.core.domain.alert.Alert;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/incidents")
public class IncidentController {

    private final GetIncidentByIdUseCase getIncidentByIdUseCase;
    private final ListIncidentsUseCase listIncidentsUseCase;
    private final ListIncidentEvidenceUseCase listIncidentEvidenceUseCase;
    private final AcknowledgeIncidentUseCase acknowledgeIncidentUseCase;
    private final ResolveIncidentUseCase resolveIncidentUseCase;
    private final ListIncidentTimelineUseCase listIncidentTimelineUseCase;
    private final ListIncidentAlertsUseCase listIncidentAlertsUseCase;

    public IncidentController(
            GetIncidentByIdUseCase getIncidentByIdUseCase,
            ListIncidentsUseCase listIncidentsUseCase,
            ListIncidentEvidenceUseCase listIncidentEvidenceUseCase,
            AcknowledgeIncidentUseCase acknowledgeIncidentUseCase,
            ResolveIncidentUseCase resolveIncidentUseCase,
            ListIncidentTimelineUseCase listIncidentTimelineUseCase,
            ListIncidentAlertsUseCase listIncidentAlertsUseCase
    ) {
        this.getIncidentByIdUseCase = getIncidentByIdUseCase;
        this.listIncidentsUseCase = listIncidentsUseCase;
        this.listIncidentEvidenceUseCase = listIncidentEvidenceUseCase;
        this.acknowledgeIncidentUseCase = acknowledgeIncidentUseCase;
        this.resolveIncidentUseCase = resolveIncidentUseCase;
        this.listIncidentTimelineUseCase = listIncidentTimelineUseCase;
        this.listIncidentAlertsUseCase = listIncidentAlertsUseCase;
    }

    @GetMapping
    public List<IncidentResponse> listIncidents(
            @RequestParam(name = "status", required = false) IncidentStatus status,
            @RequestParam(name = "limit", required = false) Integer limit
    ) {
        return listIncidentsUseCase.list(status, limit)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @GetMapping("/{incidentId}")
    public IncidentResponse getIncidentById(@PathVariable("incidentId") UUID incidentId) {
        Incident incident = getIncidentByIdUseCase.findById(IncidentId.from(incidentId))
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Incident not found"));

        return toResponse(incident);
    }

    @GetMapping("/{incidentId}/evidence")
    public List<IncidentEvidenceResponse> getIncidentEvidence(@PathVariable("incidentId") UUID incidentId) {
        return listIncidentEvidenceUseCase.findByIncidentId(IncidentId.from(incidentId))
                .stream()
                .map(this::toEvidenceResponse)
                .toList();
    }

    @PostMapping("/{incidentId}/acknowledge")
    public IncidentResponse acknowledgeIncident(@PathVariable("incidentId") UUID incidentId) {
        try {
            Incident incident = acknowledgeIncidentUseCase.acknowledge(IncidentId.from(incidentId))
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Incident not found"));

            return toResponse(incident);
        } catch (IllegalStateException exception) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, exception.getMessage());
        }
    }

    @PostMapping("/{incidentId}/resolve")
    public IncidentResponse resolveIncident(@PathVariable("incidentId") UUID incidentId) {
        try {
            Incident incident = resolveIncidentUseCase.resolve(IncidentId.from(incidentId))
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Incident not found"));

            return toResponse(incident);
        } catch (IllegalStateException exception) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, exception.getMessage());
        }
    }

    @GetMapping("/{incidentId}/timeline")
    public List<IncidentTimelineEventResponse> getIncidentTimeline(@PathVariable("incidentId") UUID incidentId) {
        return listIncidentTimelineUseCase.findByIncidentId(IncidentId.from(incidentId))
                .stream()
                .map(this::toTimelineResponse)
                .toList();
    }

    @GetMapping("/{incidentId}/alerts")
    public List<IncidentAlertResponse> getIncidentAlerts(@PathVariable("incidentId") UUID incidentId) {
        return listIncidentAlertsUseCase.findByIncidentId(IncidentId.from(incidentId))
                .stream()
                .map(this::toAlertResponse)
                .toList();
    }

    private IncidentResponse toResponse(Incident incident) {
        return new IncidentResponse(
                incident.id().value().toString(),
                incident.serviceName(),
                incident.environment(),
                incident.type(),
                incident.severity(),
                incident.status(),
                incident.summary(),
                incident.description(),
                incident.deduplicationKey().value(),
                incident.occurrenceCount(),
                incident.firstSeenAt(),
                incident.lastSeenAt(),
                incident.openedAt(),
                incident.acknowledgedAt(),
                incident.resolvedAt()
        );
    }

    private IncidentEvidenceResponse toEvidenceResponse(IncidentEvidence evidence) {
        return new IncidentEvidenceResponse(
                evidence.id().value().toString(),
                evidence.incidentId().value().toString(),
                evidence.signalId().value().toString(),
                evidence.ruleId().value().toString(),
                evidence.capturedAt(),
                evidence.summary(),
                evidence.attributes()
        );
    }

    private IncidentTimelineEventResponse toTimelineResponse(IncidentTimelineEvent event) {
        return new IncidentTimelineEventResponse(
                event.id().value().toString(),
                event.incidentId().value().toString(),
                event.type(),
                event.occurredAt(),
                event.summary(),
                event.actor(),
                event.attributes()
        );
    }

    private IncidentAlertResponse toAlertResponse(Alert alert) {
        return new IncidentAlertResponse(
                alert.id().value().toString(),
                alert.incidentId().value().toString(),
                alert.channel(),
                alert.status(),
                alert.title(),
                alert.message(),
                alert.createdAt(),
                alert.sentAt(),
                alert.failedAt(),
                alert.failureReason(),
                alert.attributes()
        );
    }
}