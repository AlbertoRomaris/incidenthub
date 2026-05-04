package com.incidenthub.core.application.usecase;

import com.incidenthub.core.application.model.OperationalMetricsSummary;
import com.incidenthub.core.application.model.SignalProcessingTaskStatus;
import com.incidenthub.core.application.port.AlertRepository;
import com.incidenthub.core.application.port.IncidentRepository;
import com.incidenthub.core.application.port.SignalProcessingTaskRepository;
import com.incidenthub.core.domain.alert.AlertStatus;
import com.incidenthub.core.domain.incident.IncidentSeverity;
import com.incidenthub.core.domain.incident.IncidentStatus;
import com.incidenthub.core.domain.metrics.OperationalMetric;
import com.incidenthub.core.domain.metrics.OperationalMetricName;
import com.incidenthub.core.domain.metrics.OperationalMetricUnit;
import com.incidenthub.core.domain.metrics.SloDefinition;
import com.incidenthub.core.domain.metrics.SloEvaluation;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

public class GetOperationalMetricsSummaryUseCase {

    private final SignalProcessingTaskRepository signalProcessingTaskRepository;
    private final IncidentRepository incidentRepository;
    private final AlertRepository alertRepository;
    private final Clock clock;

    public GetOperationalMetricsSummaryUseCase(
            SignalProcessingTaskRepository signalProcessingTaskRepository,
            IncidentRepository incidentRepository,
            AlertRepository alertRepository,
            Clock clock
    ) {
        this.signalProcessingTaskRepository = Objects.requireNonNull(
                signalProcessingTaskRepository,
                "Signal processing task repository must not be null"
        );
        this.incidentRepository = Objects.requireNonNull(incidentRepository, "Incident repository must not be null");
        this.alertRepository = Objects.requireNonNull(alertRepository, "Alert repository must not be null");
        this.clock = Objects.requireNonNull(clock, "Clock must not be null");
    }

    public OperationalMetricsSummary getSummary() {
        Instant generatedAt = Instant.now(clock);

        long pendingTasks = signalProcessingTaskRepository.countByStatus(SignalProcessingTaskStatus.PENDING);
        long processedTasks = signalProcessingTaskRepository.countByStatus(SignalProcessingTaskStatus.PROCESSED);
        long failedTasks = signalProcessingTaskRepository.countByStatus(SignalProcessingTaskStatus.FAILED);
        long totalTasks = pendingTasks + processedTasks + failedTasks;

        double signalProcessingSuccessRate = percentage(processedTasks, processedTasks + failedTasks);
        double oldestPendingTaskAgeSeconds = oldestPendingTaskAgeSeconds(generatedAt);
        Instant recentProcessingWindowStart = generatedAt.minusSeconds(15 * 60);

        double averageProcessingLatencyMs = signalProcessingTaskRepository
                .findAverageProcessedTaskLatencyMsSince(recentProcessingWindowStart)
                .orElse(0.0);

        long openIncidents = incidentRepository.countByStatus(IncidentStatus.OPEN);
        long acknowledgedIncidents = incidentRepository.countByStatus(IncidentStatus.ACKNOWLEDGED);
        long resolvedIncidents = incidentRepository.countByStatus(IncidentStatus.RESOLVED);

        long pendingAlerts = alertRepository.countByStatus(AlertStatus.PENDING);
        long sentAlerts = alertRepository.countByStatus(AlertStatus.SENT);
        long failedAlerts = alertRepository.countByStatus(AlertStatus.FAILED);
        long totalAlerts = pendingAlerts + sentAlerts + failedAlerts;

        double alertDeliverySuccessRate = percentage(sentAlerts, sentAlerts + failedAlerts);

        List<OperationalMetric> metrics = List.of(
                OperationalMetric.counter(
                        OperationalMetricName.SIGNAL_PROCESSING_TASKS_TOTAL,
                        totalTasks,
                        generatedAt
                ),
                OperationalMetric.gauge(
                        OperationalMetricName.SIGNAL_PROCESSING_TASKS_PENDING,
                        OperationalMetricUnit.COUNT,
                        pendingTasks,
                        generatedAt
                ),
                OperationalMetric.counter(
                        OperationalMetricName.SIGNAL_PROCESSING_TASKS_PROCESSED,
                        processedTasks,
                        generatedAt
                ),
                OperationalMetric.counter(
                        OperationalMetricName.SIGNAL_PROCESSING_TASKS_FAILED,
                        failedTasks,
                        generatedAt
                ),
                OperationalMetric.ratio(
                        OperationalMetricName.SIGNAL_PROCESSING_SUCCESS_RATE,
                        signalProcessingSuccessRate,
                        generatedAt
                ),
                OperationalMetric.gauge(
                        OperationalMetricName.SIGNAL_PROCESSING_AVERAGE_LATENCY_MS,
                        OperationalMetricUnit.MILLISECONDS,
                        averageProcessingLatencyMs,
                        generatedAt
                ),
                OperationalMetric.gauge(
                        OperationalMetricName.OLDEST_PENDING_TASK_AGE_SECONDS,
                        OperationalMetricUnit.SECONDS,
                        oldestPendingTaskAgeSeconds,
                        generatedAt
                ),
                OperationalMetric.gauge(
                        OperationalMetricName.INCIDENTS_OPEN,
                        OperationalMetricUnit.COUNT,
                        openIncidents,
                        generatedAt
                ),
                OperationalMetric.gauge(
                        OperationalMetricName.INCIDENTS_ACTIVE,
                        OperationalMetricUnit.COUNT,
                        openIncidents + acknowledgedIncidents,
                        generatedAt
                ),
                OperationalMetric.counter(
                        OperationalMetricName.INCIDENTS_RESOLVED,
                        resolvedIncidents,
                        generatedAt
                ),
                OperationalMetric.gauge(
                        OperationalMetricName.INCIDENTS_HIGH_OPEN,
                        OperationalMetricUnit.COUNT,
                        incidentRepository.countByStatusAndSeverity(IncidentStatus.OPEN, IncidentSeverity.HIGH),
                        generatedAt
                ),
                OperationalMetric.gauge(
                        OperationalMetricName.INCIDENTS_CRITICAL_OPEN,
                        OperationalMetricUnit.COUNT,
                        incidentRepository.countByStatusAndSeverity(IncidentStatus.OPEN, IncidentSeverity.CRITICAL),
                        generatedAt
                ),
                OperationalMetric.counter(
                        OperationalMetricName.ALERTS_TOTAL,
                        totalAlerts,
                        generatedAt
                ),
                OperationalMetric.gauge(
                        OperationalMetricName.ALERTS_PENDING,
                        OperationalMetricUnit.COUNT,
                        pendingAlerts,
                        generatedAt
                ),
                OperationalMetric.counter(
                        OperationalMetricName.ALERTS_SENT,
                        sentAlerts,
                        generatedAt
                ),
                OperationalMetric.counter(
                        OperationalMetricName.ALERTS_FAILED,
                        failedAlerts,
                        generatedAt
                ),
                OperationalMetric.ratio(
                        OperationalMetricName.ALERT_DELIVERY_SUCCESS_RATE,
                        alertDeliverySuccessRate,
                        generatedAt
                )
        );

        List<SloEvaluation> sloEvaluations = List.of(
                SloDefinition.signalProcessingSuccessRate().evaluate(
                        findMetric(metrics, OperationalMetricName.SIGNAL_PROCESSING_SUCCESS_RATE),
                        generatedAt
                ),
                SloDefinition.alertDeliverySuccessRate().evaluate(
                        findMetric(metrics, OperationalMetricName.ALERT_DELIVERY_SUCCESS_RATE),
                        generatedAt
                ),
                SloDefinition.pendingTaskBacklog().evaluate(
                        findMetric(metrics, OperationalMetricName.SIGNAL_PROCESSING_TASKS_PENDING),
                        generatedAt
                ),
                SloDefinition.oldestPendingTaskAge().evaluate(
                        findMetric(metrics, OperationalMetricName.OLDEST_PENDING_TASK_AGE_SECONDS),
                        generatedAt
                ),
                SloDefinition.signalProcessingAverageLatency().evaluate(
                        findMetric(metrics, OperationalMetricName.SIGNAL_PROCESSING_AVERAGE_LATENCY_MS),
                        generatedAt
                )
        );

        return new OperationalMetricsSummary(
                generatedAt,
                metrics,
                sloEvaluations
        );
    }

    private double oldestPendingTaskAgeSeconds(Instant generatedAt) {
        Optional<Instant> oldestPendingTaskCreatedAt = signalProcessingTaskRepository.findOldestPendingTaskCreatedAt();

        return oldestPendingTaskCreatedAt
                .map(createdAt -> Duration.between(createdAt, generatedAt).toSeconds())
                .orElse(0L);
    }

    private double percentage(long numerator, long denominator) {
        if (denominator == 0) {
            return 100.0;
        }

        return (numerator * 100.0) / denominator;
    }

    private OperationalMetric findMetric(
            List<OperationalMetric> metrics,
            OperationalMetricName metricName
    ) {
        return metrics.stream()
                .filter(metric -> metric.name() == metricName)
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("Metric not found: " + metricName));
    }
}