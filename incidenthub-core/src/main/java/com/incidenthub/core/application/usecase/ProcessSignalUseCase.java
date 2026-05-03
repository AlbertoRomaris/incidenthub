package com.incidenthub.core.application.usecase;

import com.incidenthub.core.application.model.ProcessSignalResult;
import com.incidenthub.core.application.port.IncidentEvidenceRepository;
import com.incidenthub.core.application.port.IncidentRepository;
import com.incidenthub.core.application.port.IncidentTimelineRepository;
import com.incidenthub.core.application.port.RuleRepository;
import com.incidenthub.core.application.port.SignalRepository;
import com.incidenthub.core.domain.evidence.IncidentEvidence;
import com.incidenthub.core.domain.incident.DeduplicationKey;
import com.incidenthub.core.domain.incident.Incident;
import com.incidenthub.core.domain.incident.IncidentId;
import com.incidenthub.core.domain.rule.Rule;
import com.incidenthub.core.domain.rule.RuleConditionType;
import com.incidenthub.core.domain.rule.RuleEvaluationResult;
import com.incidenthub.core.domain.runbook.RunbookReference;
import com.incidenthub.core.domain.signal.Signal;
import com.incidenthub.core.domain.signal.SignalId;
import com.incidenthub.core.domain.timeline.IncidentTimelineEvent;
import com.incidenthub.core.domain.timeline.IncidentTimelineEventType;

import java.time.Clock;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class ProcessSignalUseCase {

    private final SignalRepository signalRepository;
    private final RuleRepository ruleRepository;
    private final IncidentRepository incidentRepository;
    private final IncidentEvidenceRepository incidentEvidenceRepository;
    private final IncidentTimelineRepository incidentTimelineRepository;
    private final Clock clock;

    public ProcessSignalUseCase(
            SignalRepository signalRepository,
            RuleRepository ruleRepository,
            IncidentRepository incidentRepository,
            IncidentEvidenceRepository incidentEvidenceRepository,
            IncidentTimelineRepository incidentTimelineRepository,
            Clock clock
    ) {
        this.signalRepository = Objects.requireNonNull(signalRepository, "Signal repository must not be null");
        this.ruleRepository = Objects.requireNonNull(ruleRepository, "Rule repository must not be null");
        this.incidentRepository = Objects.requireNonNull(incidentRepository, "Incident repository must not be null");
        this.incidentEvidenceRepository = Objects.requireNonNull(incidentEvidenceRepository, "Incident evidence repository must not be null");
        this.incidentTimelineRepository = Objects.requireNonNull(incidentTimelineRepository, "Incident timeline repository must not be null");
        this.clock = Objects.requireNonNull(clock, "Clock must not be null");
    }

    public ProcessSignalResult process(SignalId signalId) {
        Objects.requireNonNull(signalId, "Signal id must not be null");

        Signal signal = signalRepository.findById(signalId)
                .orElseThrow(() -> new IllegalArgumentException("Signal not found"));

        Instant evaluatedAt = Instant.now(clock);

        List<Rule> applicableRules = ruleRepository.findEnabled()
                .stream()
                .filter(rule -> rule.appliesToService(signal.serviceName()))
                .filter(rule -> rule.signalType() == signal.type())
                .toList();

        List<IncidentId> affectedIncidents = new ArrayList<>();
        int matchedRules = 0;

        for (Rule rule : applicableRules) {
            RuleEvaluationResult evaluationResult = evaluate(rule, signal, evaluatedAt);

            if (!evaluationResult.matched()) {
                continue;
            }

            matchedRules++;

            IncidentDecision incidentDecision = openOrUpdateIncident(
                    signal,
                    rule,
                    evaluationResult,
                    evaluatedAt
            );

            Incident savedIncident = incidentRepository.save(incidentDecision.incident());

            if (incidentDecision.newIncident()) {
                recordIncidentOpened(savedIncident, rule, evaluationResult, evaluatedAt);
                recordRunbookIfAvailable(savedIncident, evaluatedAt);
            }

            IncidentEvidence evidence = createEvidence(
                    savedIncident,
                    signal,
                    rule,
                    evaluationResult,
                    evaluatedAt
            );

            IncidentEvidence savedEvidence = incidentEvidenceRepository.save(evidence);
            recordEvidenceAttached(savedIncident, savedEvidence, rule, evaluatedAt);

            affectedIncidents.add(savedIncident.id());
        }

        return ProcessSignalResult.of(
                signal.id(),
                applicableRules.size(),
                matchedRules,
                affectedIncidents
        );
    }

    private RuleEvaluationResult evaluate(
            Rule rule,
            Signal signal,
            Instant evaluatedAt
    ) {
        if (rule.conditionType() != RuleConditionType.COUNT_OVER_WINDOW) {
            return RuleEvaluationResult.notMatched(
                    rule,
                    0,
                    evaluatedAt,
                    "Unsupported rule condition type in current version"
            );
        }

        Instant since = evaluatedAt.minusSeconds(rule.timeWindowSeconds());

        List<Signal> matchingSignals = signalRepository.findByServiceAndTypeSince(
                signal.serviceName(),
                rule.signalType(),
                since,
                rule.threshold()
        );

        int matchingSignalsCount = matchingSignals.size();

        if (matchingSignalsCount >= rule.threshold()) {
            return RuleEvaluationResult.matched(
                    rule,
                    matchingSignalsCount,
                    evaluatedAt,
                    "Matched " + matchingSignalsCount + " signals within " + rule.timeWindowSeconds() + " seconds"
            );
        }

        return RuleEvaluationResult.notMatched(
                rule,
                matchingSignalsCount,
                evaluatedAt,
                "Only " + matchingSignalsCount + " signals found within " + rule.timeWindowSeconds() + " seconds"
        );
    }

    private IncidentDecision openOrUpdateIncident(
            Signal signal,
            Rule rule,
            RuleEvaluationResult evaluationResult,
            Instant evaluatedAt
    ) {
        DeduplicationKey deduplicationKey = DeduplicationKey.from(
                signal.environment(),
                signal.serviceName(),
                rule.incidentType()
        );

        return incidentRepository.findActiveByDeduplicationKey(deduplicationKey)
                .map(existingIncident -> new IncidentDecision(
                        existingIncident.recordOccurrence(evaluatedAt),
                        false
                ))
                .orElseGet(() -> new IncidentDecision(
                        Incident.open(
                                signal.serviceName(),
                                signal.environment(),
                                rule.incidentType(),
                                rule.incidentSeverity(),
                                buildIncidentSummary(signal, rule),
                                buildIncidentDescription(rule, evaluationResult),
                                evaluatedAt
                        ),
                        true
                ));
    }

    private IncidentEvidence createEvidence(
            Incident incident,
            Signal signal,
            Rule rule,
            RuleEvaluationResult evaluationResult,
            Instant capturedAt
    ) {
        return IncidentEvidence.captured(
                incident.id(),
                signal.id(),
                rule.id(),
                capturedAt,
                "Signal matched rule: " + rule.name(),
                Map.of(
                        "serviceName", signal.serviceName(),
                        "environment", signal.environment(),
                        "signalType", signal.type().name(),
                        "severity", signal.severity().name(),
                        "correlationId", signal.correlationId() == null ? "" : signal.correlationId(),
                        "matchingSignalsCount", evaluationResult.matchingSignalsCount(),
                        "reason", evaluationResult.reason()
                )
        );
    }

    private void recordIncidentOpened(
            Incident incident,
            Rule rule,
            RuleEvaluationResult evaluationResult,
            Instant occurredAt
    ) {
        incidentTimelineRepository.save(IncidentTimelineEvent.record(
                incident.id(),
                IncidentTimelineEventType.INCIDENT_OPENED,
                occurredAt,
                "Incident opened by rule: " + rule.name(),
                "worker",
                Map.of(
                        "ruleId", rule.id().value().toString(),
                        "incidentType", incident.type().name(),
                        "severity", incident.severity().name(),
                        "matchingSignalsCount", evaluationResult.matchingSignalsCount(),
                        "reason", evaluationResult.reason()
                )
        ));
    }

    private void recordRunbookIfAvailable(
            Incident incident,
            Instant occurredAt
    ) {
        RunbookReference.forIncidentType(incident.type())
                .ifPresent(runbook -> incidentTimelineRepository.save(IncidentTimelineEvent.record(
                        incident.id(),
                        IncidentTimelineEventType.RUNBOOK_ATTACHED,
                        occurredAt,
                        "Runbook attached: " + runbook.title(),
                        "system",
                        Map.of(
                                "incidentType", incident.type().name(),
                                "title", runbook.title(),
                                "documentPath", runbook.documentPath(),
                                "summary", runbook.summary()
                        )
                )));
    }

    private void recordEvidenceAttached(
            Incident incident,
            IncidentEvidence evidence,
            Rule rule,
            Instant occurredAt
    ) {
        incidentTimelineRepository.save(IncidentTimelineEvent.record(
                incident.id(),
                IncidentTimelineEventType.EVIDENCE_ATTACHED,
                occurredAt,
                "Evidence attached from signal: " + evidence.signalId().value(),
                "worker",
                Map.of(
                        "evidenceId", evidence.id().value().toString(),
                        "signalId", evidence.signalId().value().toString(),
                        "ruleId", rule.id().value().toString()
                )
        ));
    }

    private String buildIncidentSummary(Signal signal, Rule rule) {
        return rule.incidentType().name() + " detected for " + signal.serviceName();
    }

    private String buildIncidentDescription(
            Rule rule,
            RuleEvaluationResult evaluationResult
    ) {
        return rule.description()
                + " "
                + evaluationResult.reason();
    }

    private record IncidentDecision(
            Incident incident,
            boolean newIncident
    ) {
    }
}