package com.incidenthub.infrastructure.persistence.task;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

interface SpringDataSignalProcessingTaskRepository extends JpaRepository<SignalProcessingTaskEntity, UUID> {

    @Query(
            value = """
                    SELECT *
                    FROM signal_processing_tasks
                    WHERE status = 'PENDING'
                    ORDER BY created_at ASC
                    LIMIT :limit
                    FOR UPDATE SKIP LOCKED
                    """,
            nativeQuery = true
    )
    List<SignalProcessingTaskEntity> findPendingForUpdate(@Param("limit") int limit);
}