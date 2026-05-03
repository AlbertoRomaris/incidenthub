package com.incidenthub.api.controller;

import com.incidenthub.api.dto.rule.RuleResponse;
import com.incidenthub.core.application.usecase.GetRuleByIdUseCase;
import com.incidenthub.core.application.usecase.ListEnabledRulesUseCase;
import com.incidenthub.core.domain.rule.Rule;
import com.incidenthub.core.domain.rule.RuleId;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/rules")
public class RuleController {

    private final ListEnabledRulesUseCase listEnabledRulesUseCase;
    private final GetRuleByIdUseCase getRuleByIdUseCase;

    public RuleController(
            ListEnabledRulesUseCase listEnabledRulesUseCase,
            GetRuleByIdUseCase getRuleByIdUseCase
    ) {
        this.listEnabledRulesUseCase = listEnabledRulesUseCase;
        this.getRuleByIdUseCase = getRuleByIdUseCase;
    }

    @GetMapping
    public List<RuleResponse> listEnabledRules() {
        return listEnabledRulesUseCase.listEnabled()
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @GetMapping("/{ruleId}")
    public RuleResponse getRuleById(@PathVariable("ruleId") UUID ruleId) {
        Rule rule = getRuleByIdUseCase.findById(RuleId.from(ruleId))
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Rule not found"));

        return toResponse(rule);
    }

    private RuleResponse toResponse(Rule rule) {
        return new RuleResponse(
                rule.id().value().toString(),
                rule.name(),
                rule.description(),
                rule.servicePattern(),
                rule.signalType(),
                rule.conditionType(),
                rule.threshold(),
                rule.timeWindowSeconds(),
                rule.incidentType(),
                rule.incidentSeverity(),
                rule.enabled()
        );
    }
}