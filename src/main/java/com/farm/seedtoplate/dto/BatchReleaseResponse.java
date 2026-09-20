package com.farm.seedtoplate.dto;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record BatchReleaseResponse(
    UUID id,
    UUID batchId,
    BigDecimal releasedKg,
    BigDecimal pricePerKg,
    Instant releasedAt,
    String note
) {}
