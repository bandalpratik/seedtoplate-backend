package com.farm.seedtoplate.dto;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record ManifestResponse(
    UUID id,
    UUID batchId,
    String zone,
    String route,
    BigDecimal loadKg,
    String handoffStatus,
    Instant createdAt
) {}
