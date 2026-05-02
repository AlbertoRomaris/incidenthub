package com.incidenthub.infrastructure.persistence.signal;

import com.incidenthub.core.application.port.SignalRepository;
import com.incidenthub.core.domain.signal.Signal;
import com.incidenthub.core.domain.signal.SignalId;
import com.incidenthub.core.domain.signal.SignalType;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

@Repository
public class JpaSignalRepository implements SignalRepository {

    private final SpringDataSignalRepository springDataSignalRepository;

    public JpaSignalRepository(SpringDataSignalRepository springDataSignalRepository) {
        this.springDataSignalRepository = springDataSignalRepository;
    }

    @Override
    public Signal save(Signal signal) {
        SignalEntity savedEntity = springDataSignalRepository.save(SignalEntity.fromDomain(signal));
        return savedEntity.toDomain();
    }

    @Override
    public Optional<Signal> findById(SignalId signalId) {
        return springDataSignalRepository.findById(signalId.value())
                .map(SignalEntity::toDomain);
    }

    @Override
    public List<Signal> findRecent(int limit) {
        PageRequest pageRequest = PageRequest.of(
                0,
                limit,
                Sort.by(Sort.Direction.DESC, "receivedAt")
        );

        return springDataSignalRepository.findAll(pageRequest)
                .stream()
                .map(SignalEntity::toDomain)
                .toList();
    }

    @Override
    public List<Signal> findByServiceAndTypeSince(
            String serviceName,
            SignalType signalType,
            Instant since,
            int limit
    ) {
        PageRequest pageRequest = PageRequest.of(
                0,
                limit,
                Sort.by(Sort.Direction.DESC, "observedAt")
        );

        return springDataSignalRepository
                .findByServiceNameAndTypeAndObservedAtGreaterThanEqual(
                        serviceName,
                        signalType,
                        since,
                        pageRequest
                )
                .stream()
                .map(SignalEntity::toDomain)
                .toList();
    }
}