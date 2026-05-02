package com.incidenthub.infrastructure.persistence.rule;

import com.incidenthub.core.application.port.RuleRepository;
import com.incidenthub.core.domain.rule.Rule;
import com.incidenthub.core.domain.rule.RuleId;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public class JpaRuleRepository implements RuleRepository {

    private final SpringDataRuleRepository springDataRuleRepository;

    public JpaRuleRepository(SpringDataRuleRepository springDataRuleRepository) {
        this.springDataRuleRepository = springDataRuleRepository;
    }

    @Override
    public List<Rule> findEnabled() {
        return springDataRuleRepository.findByEnabledTrue()
                .stream()
                .map(RuleEntity::toDomain)
                .toList();
    }

    @Override
    public Optional<Rule> findById(RuleId ruleId) {
        return springDataRuleRepository.findById(ruleId.value())
                .map(RuleEntity::toDomain);
    }
}