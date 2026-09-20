package com.farm.seedtoplate.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record OtpVerificationRequest(
    @NotBlank @Pattern(regexp = "\\d{10}") String phone,
    @NotBlank String otpCode
) {}
