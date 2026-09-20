package com.farm.seedtoplate.dto;

import jakarta.validation.constraints.NotNull;
import java.util.UUID;

public record PaymentCreateRequest(@NotNull UUID reservationId, String returnBaseUrl) {}
