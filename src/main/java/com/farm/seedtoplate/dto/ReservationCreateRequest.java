package com.farm.seedtoplate.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.util.UUID;

public record ReservationCreateRequest(
    @NotNull UUID batchId,
    @NotNull @DecimalMin("0.01") BigDecimal reservedKg,
    String pickupLocation,
    BigDecimal expectedPriceLowPerKg,
    BigDecimal expectedPriceHighPerKg
) {}
