package com.incidenthub.core.domain.rule;

import java.time.Instant;
import java.util.Objects;

public record RuleEvaluationResult(
        Rule rule,
        boolean matched,
        int matchingSignalsCount,
        Instant evaluatedAt,
        String reason
) {

    public RuleEvaluationResult {
        Objects.requireNonNull(rule, "Rule must not be null");
        Objects.requireNonNull(evaluatedAt, "Evaluation timestamp must not be null");
        reason = reason == null ? "" : reason.trim();

        if (matchingSignalsCount < 0) {
            throw new IllegalArgumentException("Matching signals count must not be negative");
        }
    }

    public static RuleEvaluationResult matched(
            Rule rule,
            int matchingSignalsCount,
            Instant evaluatedAt,
            String reason
    ) {
        return new RuleEvaluationResult(
                rule,
                true,
                matchingSignalsCount,
                evaluatedAt,
                reason
        );
    }

    public static RuleEvaluationResult notMatched(
            Rule rule,
            int matchingSignalsCount,
            Instant evaluatedAt,
            String reason
    ) {
        return new RuleEvaluationResult(
                rule,
                false,
                matchingSignalsCount,
                evaluatedAt,
                reason
        );
    }
}