package com.farm.seedtoplate.dto;

import com.farm.seedtoplate.domain.CropStage;
import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

public record AdminBatchDetailResponse(
    UUID id,
    String cropName,
    String farmName,
    CropStage currentStage,
    BigDecimal plannedYieldKg,
    BigDecimal actualYieldKg,
    BigDecimal availableYieldKg,
    BigDecimal releasedKg,
    BigDecimal estimatedPriceLowPerKg,
    BigDecimal estimatedPriceHighPerKg,
    BigDecimal finalRetailPricePerKg,
    String description,
    String variety,
    List<BatchReleaseResponse> releases,
    long queueDepth
) {}
