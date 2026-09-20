package com.farm.seedtoplate.dto;

import com.farm.seedtoplate.domain.AuditEventType;
import java.time.Instant;
import java.util.UUID;

public record AuditEventResponse(
    UUID id,
    String entityType,
    UUID entityId,
    AuditEventType eventType,
    String message,
    Instant createdAt
) {}
