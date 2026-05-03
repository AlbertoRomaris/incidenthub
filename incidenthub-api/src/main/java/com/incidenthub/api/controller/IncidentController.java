package com.incidenthub.api.controller;

import com.incidenthub.api.dto.incident.IncidentEvidenceResponse;
import com.incidenthub.api.dto.incident.IncidentResponse;
import com.incidenthub.core.application.usecase.GetIncidentByIdUseCase;
import com.incidenthub.core.application.usecase.ListIncidentEvidenceUseCase;
import com.incidenthub.core.application.usecase.ListOpenIncidentsUseCase;
import com.incidenthub.core.domain.evidence.IncidentEvidence;
import com.incidenthub.core.domain.incident.Incident;
import com.incidenthub.core.domain.incident.IncidentId;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/incidents")
public class IncidentController {

    private final GetIncidentByIdUseCase getIncidentByIdUseCase;
    private final ListOpenIncidentsUseCase listOpenIncidentsUseCase;
    private final ListIncidentEvidenceUseCase listIncidentEvidenceUseCase;

    public IncidentController(
            GetIncidentByIdUseCase getIncidentByIdUseCase,
            ListOpenIncidentsUseCase listOpenIncidentsUseCase,
            ListIncidentEvidenceUseCase listIncidentEvidenceUseCase
    ) {
        this.getIncidentByIdUseCase = getIncidentByIdUseCase;
        this.listOpenIncidentsUseCase = listOpenIncidentsUseCase;
        this.listIncidentEvidenceUseCase = listIncidentEvidenceUseCase;
    }

    @GetMapping
    public List<IncidentResponse> listOpenIncidents(
            @RequestParam(name = "limit", required = false) Integer limit
    ) {
        return listOpenIncidentsUseCase.listOpen(limit)
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
}