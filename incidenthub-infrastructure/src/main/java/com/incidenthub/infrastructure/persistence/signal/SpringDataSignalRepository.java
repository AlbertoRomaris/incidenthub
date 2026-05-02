package com.incidenthub.infrastructure.persistence.signal;

import com.incidenthub.core.domain.signal.SignalType;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

interface SpringDataSignalRepository extends JpaRepository<SignalEntity, UUID> {

    List<SignalEntity> findByServiceNameAndTypeAndObservedAtGreaterThanEqual(
            String serviceName,
            SignalType type,
            Instant observedAt,
            Pageable pageable
    );
}