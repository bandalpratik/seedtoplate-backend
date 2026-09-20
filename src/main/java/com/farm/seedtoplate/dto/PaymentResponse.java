package com.farm.seedtoplate.dto;

import com.farm.seedtoplate.domain.PaymentStatus;
import java.math.BigDecimal;
import java.util.UUID;

public record PaymentResponse(
    UUID id,
    UUID reservationId,
    BigDecimal amount,
    PaymentStatus status,
    String providerRef,
    String paymentLink
) {}
