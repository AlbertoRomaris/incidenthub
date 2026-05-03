package com.incidenthub.core.application.usecase;

import com.incidenthub.core.application.port.RuleRepository;
import com.incidenthub.core.domain.rule.Rule;

import java.util.List;
import java.util.Objects;

public class ListEnabledRulesUseCase {

    private final RuleRepository ruleRepository;

    public ListEnabledRulesUseCase(RuleRepository ruleRepository) {
        this.ruleRepository = Objects.requireNonNull(ruleRepository, "Rule repository must not be null");
    }

    public List<Rule> listEnabled() {
        return ruleRepository.findEnabled();
    }
}