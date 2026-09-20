package com.farm.seedtoplate.dto;

import com.farm.seedtoplate.domain.ReservationStatus;
import com.farm.seedtoplate.domain.FulfillmentStatus;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record ReservationResponse(
    UUID id,
    UUID batchId,
    UUID userId,
    UUID releaseId,
    BigDecimal reservedKg,
    ReservationStatus status,
    BigDecimal pricePerKg,
    String pickupLocation,
    Instant paymentDueAt,
    FulfillmentStatus fulfillmentStatus,
    BigDecimal expectedPriceLowPerKg,
    BigDecimal expectedPriceHighPerKg,
    String cancelReason,
    Instant createdAt
) {}
