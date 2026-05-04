package com.incidenthub.api.controller;

import com.incidenthub.api.dto.metrics.OperationalMetricResponse;
import com.incidenthub.api.dto.metrics.OperationalMetricsResponse;
import com.incidenthub.api.dto.metrics.SloEvaluationResponse;
import com.incidenthub.api.dto.metrics.SloSummaryResponse;
import com.incidenthub.api.metrics.PrometheusMetricsFormatter;
import com.incidenthub.core.application.model.OperationalMetricsSummary;
import com.incidenthub.core.application.usecase.GetOperationalMetricsSummaryUseCase;
import com.incidenthub.core.domain.metrics.OperationalMetric;
import com.incidenthub.core.domain.metrics.SloEvaluation;
import com.incidenthub.core.domain.metrics.SloStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class MetricsController {

    private final GetOperationalMetricsSummaryUseCase getOperationalMetricsSummaryUseCase;
    private final PrometheusMetricsFormatter prometheusMetricsFormatter;

    public MetricsController(
            GetOperationalMetricsSummaryUseCase getOperationalMetricsSummaryUseCase,
            PrometheusMetricsFormatter prometheusMetricsFormatter
    ) {
        this.getOperationalMetricsSummaryUseCase = getOperationalMetricsSummaryUseCase;
        this.prometheusMetricsFormatter = prometheusMetricsFormatter;
    }

    @GetMapping("/metrics/operational")
    public OperationalMetricsResponse getOperationalMetrics() {
        OperationalMetricsSummary summary = getOperationalMetricsSummaryUseCase.getSummary();

        return new OperationalMetricsResponse(
                summary.generatedAt(),
                summary.metrics()
                        .stream()
                        .map(this::toMetricResponse)
                        .toList()
        );
    }

    @GetMapping("/slo/summary")
    public SloSummaryResponse getSloSummary() {
        OperationalMetricsSummary summary = getOperationalMetricsSummaryUseCase.getSummary();

        long healthySlos = summary.sloEvaluations()
                .stream()
                .filter(evaluation -> evaluation.status() == SloStatus.HEALTHY)
                .count();

        long breachedSlos = summary.sloEvaluations()
                .stream()
                .filter(evaluation -> evaluation.status() == SloStatus.BREACHED)
                .count();

        return new SloSummaryResponse(
                summary.generatedAt(),
                healthySlos,
                breachedSlos,
                summary.sloEvaluations()
                        .stream()
                        .map(this::toSloEvaluationResponse)
                        .toList()
        );
    }

    @GetMapping(value = "/metrics/prometheus", produces = MediaType.TEXT_PLAIN_VALUE)
    public String getPrometheusMetrics() {
        OperationalMetricsSummary summary = getOperationalMetricsSummaryUseCase.getSummary();

        return prometheusMetricsFormatter.format(summary);
    }

    private OperationalMetricResponse toMetricResponse(OperationalMetric metric) {
        return new OperationalMetricResponse(
                metric.name(),
                metric.type(),
                metric.unit(),
                metric.value(),
                metric.measuredAt(),
                metric.attributes()
        );
    }

    private SloEvaluationResponse toSloEvaluationResponse(SloEvaluation evaluation) {
        return new SloEvaluationResponse(
                evaluation.key(),
                evaluation.description(),
                evaluation.metricName(),
                evaluation.actualValue(),
                evaluation.targetValue(),
                evaluation.unit(),
                evaluation.status(),
                evaluation.healthy(),
                evaluation.breached(),
                evaluation.evaluatedAt()
        );
    }
}