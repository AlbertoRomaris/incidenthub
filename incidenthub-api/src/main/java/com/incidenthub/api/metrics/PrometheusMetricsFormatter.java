package com.incidenthub.api.metrics;

import com.incidenthub.core.application.model.OperationalMetricsSummary;
import com.incidenthub.core.domain.metrics.OperationalMetric;
import com.incidenthub.core.domain.metrics.OperationalMetricName;
import com.incidenthub.core.domain.metrics.OperationalMetricType;
import com.incidenthub.core.domain.metrics.SloEvaluation;
import org.springframework.stereotype.Component;

import java.util.Locale;

@Component
public class PrometheusMetricsFormatter {

    public String format(OperationalMetricsSummary summary) {
        StringBuilder builder = new StringBuilder();

        builder.append("# HELP incidenthub_metrics_generated_at_seconds Unix timestamp when the metrics snapshot was generated.\n");
        builder.append("# TYPE incidenthub_metrics_generated_at_seconds gauge\n");
        builder.append("incidenthub_metrics_generated_at_seconds ")
                .append(summary.generatedAt().getEpochSecond())
                .append("\n\n");

        for (OperationalMetric metric : summary.metrics()) {
            appendMetric(builder, metric);
        }

        appendSloMetrics(builder, summary);

        return builder.toString();
    }

    private void appendMetric(StringBuilder builder, OperationalMetric metric) {
        String name = toPrometheusMetricName(metric.name());

        builder.append("# HELP ")
                .append(name)
                .append(" IncidentHub operational metric ")
                .append(metric.name())
                .append(" measured in ")
                .append(metric.unit())
                .append(".\n");

        builder.append("# TYPE ")
                .append(name)
                .append(" ")
                .append(toPrometheusType(metric.type()))
                .append("\n");

        builder.append(name)
                .append(" ")
                .append(Double.toString(metric.value()))
                .append("\n\n");
    }

    private void appendSloMetrics(StringBuilder builder, OperationalMetricsSummary summary) {
        builder.append("# HELP incidenthub_slo_healthy Whether a configured IncidentHub SLO is healthy. 1 means healthy, 0 means breached.\n");
        builder.append("# TYPE incidenthub_slo_healthy gauge\n");

        for (SloEvaluation evaluation : summary.sloEvaluations()) {
            builder.append("incidenthub_slo_healthy")
                    .append("{key=\"")
                    .append(escapeLabelValue(evaluation.key()))
                    .append("\"} ")
                    .append(evaluation.healthy() ? "1.0" : "0.0")
                    .append("\n");
        }

        builder.append("\n");

        builder.append("# HELP incidenthub_slo_actual_value Current value used to evaluate a configured IncidentHub SLO.\n");
        builder.append("# TYPE incidenthub_slo_actual_value gauge\n");

        for (SloEvaluation evaluation : summary.sloEvaluations()) {
            builder.append("incidenthub_slo_actual_value")
                    .append("{key=\"")
                    .append(escapeLabelValue(evaluation.key()))
                    .append("\",metric=\"")
                    .append(toSnakeCase(evaluation.metricName().name()))
                    .append("\",unit=\"")
                    .append(evaluation.unit().name().toLowerCase(Locale.ROOT))
                    .append("\"} ")
                    .append(Double.toString(evaluation.actualValue()))
                    .append("\n");
        }

        builder.append("\n");

        builder.append("# HELP incidenthub_slo_target_value Target value configured for a configured IncidentHub SLO.\n");
        builder.append("# TYPE incidenthub_slo_target_value gauge\n");

        for (SloEvaluation evaluation : summary.sloEvaluations()) {
            builder.append("incidenthub_slo_target_value")
                    .append("{key=\"")
                    .append(escapeLabelValue(evaluation.key()))
                    .append("\",metric=\"")
                    .append(toSnakeCase(evaluation.metricName().name()))
                    .append("\",unit=\"")
                    .append(evaluation.unit().name().toLowerCase(Locale.ROOT))
                    .append("\"} ")
                    .append(Double.toString(evaluation.targetValue()))
                    .append("\n");
        }

        builder.append("\n");
    }

    private String toPrometheusMetricName(OperationalMetricName metricName) {
        return "incidenthub_" + toSnakeCase(metricName.name());
    }

    private String toPrometheusType(OperationalMetricType type) {
        return switch (type) {
            case COUNTER -> "counter";
            case GAUGE, RATIO -> "gauge";
        };
    }

    private String toSnakeCase(String value) {
        return value.toLowerCase(Locale.ROOT);
    }

    private String escapeLabelValue(String value) {
        return value
                .replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", "\\n");
    }
}