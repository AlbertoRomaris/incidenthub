package com.incidenthub.core.application.usecase;

import com.incidenthub.core.application.port.RuleRepository;
import com.incidenthub.core.domain.rule.Rule;
import com.incidenthub.core.domain.rule.RuleId;

import java.util.Objects;
import java.util.Optional;

public class GetRuleByIdUseCase {

    private final RuleRepository ruleRepository;

    public GetRuleByIdUseCase(RuleRepository ruleRepository) {
        this.ruleRepository = Objects.requireNonNull(ruleRepository, "Rule repository must not be null");
    }

    public Optional<Rule> findById(RuleId ruleId) {
        Objects.requireNonNull(ruleId, "Rule id must not be null");

        return ruleRepository.findById(ruleId);
    }
}