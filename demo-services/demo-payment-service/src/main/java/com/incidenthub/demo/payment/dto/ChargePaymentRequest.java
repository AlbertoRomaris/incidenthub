package com.incidenthub.demo.payment.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;

public record ChargePaymentRequest(
        @NotBlank
        String customerId,

        @NotNull
        @Positive
        BigDecimal amount,

        @NotBlank
        String currency,

        String correlationId
) {
}
