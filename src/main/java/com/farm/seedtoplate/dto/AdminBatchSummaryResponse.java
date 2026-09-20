package com.farm.seedtoplate.dto;

import com.farm.seedtoplate.domain.CropStage;
import java.math.BigDecimal;
import java.util.UUID;

public record AdminBatchSummaryResponse(
    UUID id,
    String cropName,
    String farmName,
    String variety,
    CropStage currentStage,
    BigDecimal plannedYieldKg,
    BigDecimal actualYieldKg,
    BigDecimal availableYieldKg,
    BigDecimal releasedKg,
    BigDecimal estimatedPriceLowPerKg,
    BigDecimal estimatedPriceHighPerKg,
    BigDecimal finalRetailPricePerKg,
    long queueDepth,
    BigDecimal queuedKg
) {}
