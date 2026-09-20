package com.farm.seedtoplate.dto;

import java.util.UUID;

public record AuthResponse(
    UUID userId,
    String phone,
    String fullName,
    String role,
    boolean isAdmin,
    String token
) {}
