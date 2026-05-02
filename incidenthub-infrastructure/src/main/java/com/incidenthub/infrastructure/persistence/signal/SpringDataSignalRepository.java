package com.incidenthub.infrastructure.persistence.signal;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

interface SpringDataSignalRepository extends JpaRepository<SignalEntity, UUID> {
}