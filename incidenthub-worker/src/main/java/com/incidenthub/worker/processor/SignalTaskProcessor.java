package com.incidenthub.worker.processor;

import com.incidenthub.core.application.model.ProcessSignalResult;
import com.incidenthub.core.application.model.SignalProcessingTask;
import com.incidenthub.core.application.port.SignalProcessingTaskRepository;
import com.incidenthub.core.application.usecase.ProcessSignalUseCase;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.Clock;
import java.time.Instant;
import java.util.List;

@Component
public class SignalTaskProcessor {

    private static final Logger log = LoggerFactory.getLogger(SignalTaskProcessor.class);

    private final SignalProcessingTaskRepository signalProcessingTaskRepository;
    private final ProcessSignalUseCase processSignalUseCase;
    private final Clock clock;
    private final String workerId;
    private final int batchSize;

    public SignalTaskProcessor(
            SignalProcessingTaskRepository signalProcessingTaskRepository,
            ProcessSignalUseCase processSignalUseCase,
            Clock clock,
            @Value("${incidenthub.worker.id:local-worker-1}") String workerId,
            @Value("${incidenthub.worker.batch-size:10}") int batchSize
    ) {
        this.signalProcessingTaskRepository = signalProcessingTaskRepository;
        this.processSignalUseCase = processSignalUseCase;
        this.clock = clock;
        this.workerId = workerId;
        this.batchSize = batchSize;
    }

    @Scheduled(fixedDelayString = "${incidenthub.worker.poll-delay-ms:5000}")
    public void pollAndProcessSignals() {
        Instant lockedAt = Instant.now(clock);

        List<SignalProcessingTask> claimedTasks = signalProcessingTaskRepository.claimPending(
                workerId,
                batchSize,
                lockedAt
        );

        if (claimedTasks.isEmpty()) {
            log.debug("No pending signal processing tasks found");
            return;
        }

        log.info("Claimed {} signal processing task(s)", claimedTasks.size());

        for (SignalProcessingTask task : claimedTasks) {
            processTask(task);
        }
    }

    private void processTask(SignalProcessingTask task) {
        try {
            ProcessSignalResult result = processSignalUseCase.process(task.signalId());

            signalProcessingTaskRepository.markProcessed(
                    task.signalId(),
                    Instant.now(clock)
            );

            log.info(
                    "Processed signal task: signalId={}, evaluatedRules={}, matchedRules={}, affectedIncidents={}",
                    result.signalId().value(),
                    result.evaluatedRules(),
                    result.matchedRules(),
                    result.affectedIncidents().size()
            );
        } catch (Exception exception) {
            signalProcessingTaskRepository.markFailed(
                    task.signalId(),
                    exception.getMessage(),
                    Instant.now(clock)
            );

            log.warn(
                    "Failed to process signal task: signalId={}, error={}",
                    task.signalId().value(),
                    exception.getMessage()
            );
        }
    }
}