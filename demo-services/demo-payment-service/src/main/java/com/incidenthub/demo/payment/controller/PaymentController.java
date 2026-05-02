package com.incidenthub.demo.payment.controller;

import com.incidenthub.demo.payment.client.IncidentHubClient;
import com.incidenthub.demo.payment.client.IncidentHubSignalRequest;
import com.incidenthub.demo.payment.dto.ChargePaymentRequest;
import com.incidenthub.demo.payment.dto.ChargePaymentResponse;
import com.incidenthub.demo.payment.simulation.FailureMode;
import com.incidenthub.demo.payment.simulation.PaymentSimulationState;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

@RestController
@RequestMapping("/payments")
public class PaymentController {

    private static final String SERVICE_NAME = "payment-service";

    private final PaymentSimulationState simulationState;
    private final IncidentHubClient incidentHubClient;
    private final String environment;

    public PaymentController(
            PaymentSimulationState simulationState,
            IncidentHubClient incidentHubClient,
            @Value("${demo-payment.environment:local}") String environment
    ) {
        this.simulationState = simulationState;
        this.incidentHubClient = incidentHubClient;
        this.environment = environment;
    }

    @PostMapping("/charge")
    public ResponseEntity<ChargePaymentResponse> charge(@Valid @RequestBody ChargePaymentRequest request) {
        String correlationId = request.correlationId() == null || request.correlationId().isBlank()
                ? "pay-" + UUID.randomUUID()
                : request.correlationId();

        FailureMode mode = simulationState.mode();

        if (mode == FailureMode.DOWN) {
            publishSignal(
                    "ERROR",
                    "CRITICAL",
                    "Payment service is unavailable",
                    correlationId,
                    null,
                    503,
                    "PAYMENT_SERVICE_DOWN",
                    Map.of("mode", mode.name())
            );

            return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                    .body(new ChargePaymentResponse(
                            null,
                            "FAILED",
                            "Payment service is unavailable",
                            correlationId
                    ));
        }

        if (mode == FailureMode.TIMEOUTS) {
            sleep(simulationState.latencyMs());

            publishSignal(
                    "TIMEOUT",
                    "HIGH",
                    "Payment provider timeout",
                    correlationId,
                    simulationState.latencyMs(),
                    504,
                    "PAYMENT_TIMEOUT",
                    Map.of("mode", mode.name())
            );

            return ResponseEntity.status(HttpStatus.GATEWAY_TIMEOUT)
                    .body(new ChargePaymentResponse(
                            null,
                            "FAILED",
                            "Payment provider timeout",
                            correlationId
                    ));
        }

        if (mode == FailureMode.HIGH_LATENCY) {
            sleep(simulationState.latencyMs());

            publishSignal(
                    "LATENCY",
                    "MEDIUM",
                    "High payment latency detected",
                    correlationId,
                    simulationState.latencyMs(),
                    200,
                    "PAYMENT_HIGH_LATENCY",
                    Map.of("mode", mode.name())
            );

            return ResponseEntity.ok(new ChargePaymentResponse(
                    "pay_" + UUID.randomUUID(),
                    "APPROVED_WITH_LATENCY",
                    "Payment approved with simulated latency",
                    correlationId
            ));
        }

        if (mode == FailureMode.RANDOM_FAILURES && shouldFail(simulationState.failureRate())) {
            publishSignal(
                    "ERROR",
                    "HIGH",
                    "Random payment provider failure",
                    correlationId,
                    null,
                    502,
                    "PAYMENT_RANDOM_FAILURE",
                    Map.of("mode", mode.name(), "failureRate", simulationState.failureRate())
            );

            return ResponseEntity.status(HttpStatus.BAD_GATEWAY)
                    .body(new ChargePaymentResponse(
                            null,
                            "FAILED",
                            "Random payment provider failure",
                            correlationId
                    ));
        }

        return ResponseEntity.ok(new ChargePaymentResponse(
                "pay_" + UUID.randomUUID(),
                "APPROVED",
                "Payment approved",
                correlationId
        ));
    }

    private boolean shouldFail(double failureRate) {
        return ThreadLocalRandom.current().nextDouble() < failureRate;
    }

    private void sleep(int latencyMs) {
        if (latencyMs <= 0) {
            return;
        }

        try {
            Thread.sleep(latencyMs);
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
        }
    }

    private void publishSignal(
            String signalType,
            String severity,
            String message,
            String correlationId,
            Integer latencyMs,
            Integer statusCode,
            String errorCode,
            Map<String, Object> attributes
    ) {
        incidentHubClient.publishSignal(new IncidentHubSignalRequest(
                SERVICE_NAME,
                environment,
                signalType,
                severity,
                message,
                correlationId,
                null,
                null,
                latencyMs,
                statusCode,
                errorCode,
                Instant.now(),
                attributes
        ));
    }
}