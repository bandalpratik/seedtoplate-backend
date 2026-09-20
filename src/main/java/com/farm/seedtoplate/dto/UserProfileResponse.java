package com.farm.seedtoplate.dto;

import java.util.UUID;

public record UserProfileResponse(
    UUID id,
    String phone,
    String fullName,
    String role,
    boolean isAdmin,
    String token
) {}
