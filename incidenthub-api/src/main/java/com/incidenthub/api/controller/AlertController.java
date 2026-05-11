package com.incidenthub.api.controller;

import com.incidenthub.api.dto.incident.IncidentAlertResponse;
import com.incidenthub.core.application.usecase.ListIncidentAlertsUseCase;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
public class AlertController {

    private final ListIncidentAlertsUseCase listIncidentAlertsUseCase;

    public AlertController(ListIncidentAlertsUseCase listIncidentAlertsUseCase) {
        this.listIncidentAlertsUseCase = listIncidentAlertsUseCase;
    }

    @GetMapping("/alerts")
    public List<IncidentAlertResponse> findRecentAlerts(
            @RequestParam(name = "limit", defaultValue = "100") int limit
    ) {
        return listIncidentAlertsUseCase.findRecent(limit)
                .stream()
                .map(IncidentAlertResponse::from)
                .toList();
    }
}