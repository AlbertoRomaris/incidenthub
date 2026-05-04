package com.incidenthub.api.dto.metrics;

import java.time.Instant;
import java.util.List;

public record SloSummaryResponse(
        Instant generatedAt,
        long healthySlos,
        long breachedSlos,
        List<SloEvaluationResponse> slos
) {
}