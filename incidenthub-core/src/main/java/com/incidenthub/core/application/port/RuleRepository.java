package com.incidenthub.core.application.port;

import com.incidenthub.core.domain.rule.Rule;
import com.incidenthub.core.domain.rule.RuleId;

import java.util.List;
import java.util.Optional;

public interface RuleRepository {

    List<Rule> findEnabled();

    Optional<Rule> findById(RuleId ruleId);
}