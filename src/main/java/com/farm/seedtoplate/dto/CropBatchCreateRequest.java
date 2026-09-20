package com.farm.seedtoplate.dto;

import com.farm.seedtoplate.domain.CropStage;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import java.math.BigDecimal;

public record CropBatchCreateRequest(
    @NotBlank String cropName,
    @NotBlank String farmName,
    String variety,
    String description,
    @DecimalMin("0.0") BigDecimal plannedYieldKg,
    @DecimalMin("0.0") BigDecimal actualYieldKg,
    @DecimalMin("0.0") BigDecimal estimatedPriceLowPerKg,
    @DecimalMin("0.0") BigDecimal estimatedPriceHighPerKg,
    @DecimalMin("0.0") BigDecimal finalRetailPricePerKg,
    CropStage currentStage
) {}
