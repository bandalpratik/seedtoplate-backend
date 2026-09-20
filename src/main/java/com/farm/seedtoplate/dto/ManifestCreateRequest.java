package com.farm.seedtoplate.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.util.UUID;

public record ManifestCreateRequest(
    @NotNull UUID batchId,
    @NotBlank String zone,
    @NotBlank String route,
    @NotNull @DecimalMin("0.01") BigDecimal loadKg
) {}
