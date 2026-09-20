package com.farm.seedtoplate.dto;

import com.farm.seedtoplate.domain.CropStage;
import java.math.BigDecimal;
import java.util.UUID;

public record CropBatchResponse(
    UUID id,
    String cropName,
    String farmName,
    CropStage currentStage,
    String description,
    String variety,
    boolean isChemicalFree,
    BigDecimal plannedYieldKg,
    BigDecimal actualYieldKg,
    BigDecimal availableYieldKg,
    BigDecimal releasedKg,
    BigDecimal estimatedPriceLowPerKg,
    BigDecimal estimatedPriceHighPerKg,
    BigDecimal finalRetailPricePerKg
) {}
