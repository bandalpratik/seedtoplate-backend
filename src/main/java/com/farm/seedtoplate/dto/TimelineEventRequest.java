package com.farm.seedtoplate.dto;

import com.farm.seedtoplate.domain.CropStage;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;

public record TimelineEventRequest(
    @NotBlank String title,
    String description,
    @NotNull CropStage stage,
    BigDecimal actualYieldKg
) {}
