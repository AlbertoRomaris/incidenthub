package com.incidenthub.demo.payment.dto;

public record ChargePaymentResponse(
        String paymentId,
        String status,
        String message,
        String correlationId
) {
}
