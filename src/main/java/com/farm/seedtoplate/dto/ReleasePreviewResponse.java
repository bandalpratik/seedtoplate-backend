package com.farm.seedtoplate.dto;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

public record ReleasePreviewResponse(
    UUID batchId,
    BigDecimal availableKg,
    BigDecimal releasedKg,
    BigDecimal pricePerKg,
    BigDecimal marginPercent,
    List<UUID> reservationIds,
    boolean bandBreach,
    String message
) {}
