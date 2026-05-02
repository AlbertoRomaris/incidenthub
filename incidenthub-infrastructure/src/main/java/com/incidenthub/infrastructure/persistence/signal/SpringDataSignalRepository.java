package com.incidenthub.infrastructure.persistence.signal;

import com.incidenthub.core.application.port.SignalRepository;
import com.incidenthub.core.domain.signal.Signal;
import com.incidenthub.core.domain.signal.SignalId;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public class SpringDataSignalRepository implements SignalRepository {

    private final JpaSignalRepository jpaSignalRepository;

    public SpringDataSignalRepository(JpaSignalRepository jpaSignalRepository) {
        this.jpaSignalRepository = jpaSignalRepository;
    }

    @Override
    public Signal save(Signal signal) {
        SignalEntity savedEntity = jpaSignalRepository.save(SignalEntity.fromDomain(signal));
        return savedEntity.toDomain();
    }

    @Override
    public Optional<Signal> findById(SignalId signalId) {
        return jpaSignalRepository.findById(signalId.value())
                .map(SignalEntity::toDomain);
    }
}