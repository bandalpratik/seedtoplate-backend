package com.farm.seedtoplate.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record AuthRequest(
    @NotBlank @Pattern(regexp = "\\d{10}") String phone,
    String fullName
) {}
