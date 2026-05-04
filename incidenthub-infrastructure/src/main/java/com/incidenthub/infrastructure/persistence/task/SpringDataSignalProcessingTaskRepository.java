package com.incidenthub.infrastructure.persistence.task;

import com.incidenthub.core.application.model.SignalProcessingTaskStatus;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
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

    long countByStatus(SignalProcessingTaskStatus status);

    @Query("""
        select task.createdAt
        from SignalProcessingTaskEntity task
        where task.status = :status
        order by task.createdAt asc
        """)
    List<Instant> findCreatedAtByStatusOrderByCreatedAtAsc(
            @Param("status") SignalProcessingTaskStatus status,
            Pageable pageable
    );
}