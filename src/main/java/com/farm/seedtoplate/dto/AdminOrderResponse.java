package com.farm.seedtoplate.dto;

import com.farm.seedtoplate.domain.FulfillmentStatus;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record AdminOrderResponse(
    UUID id,
    UUID reservationId,
    UUID customerId,
    String customerName,
    String phoneNumber,
    String cropName,
    String seedVariety,
    BigDecimal quantityKg,
    String pickupLocation,
    BigDecimal unitPrice,
    BigDecimal totalAmount,
    com.farm.seedtoplate.domain.PaymentStatus paymentStatus,
    FulfillmentStatus fulfillmentStatus,
    Instant createdAt,
    Instant paidAt
) {}
