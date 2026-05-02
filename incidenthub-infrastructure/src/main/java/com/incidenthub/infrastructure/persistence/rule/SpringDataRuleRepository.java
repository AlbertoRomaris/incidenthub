package com.incidenthub.infrastructure.persistence.rule;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

interface SpringDataRuleRepository extends JpaRepository<RuleEntity, UUID> {

    List<RuleEntity> findByEnabledTrue();
}