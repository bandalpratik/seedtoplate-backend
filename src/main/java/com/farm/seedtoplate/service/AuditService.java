package com.farm.seedtoplate.service;

import com.farm.seedtoplate.domain.AuditEvent;
import com.farm.seedtoplate.domain.AuditEventType;
import com.farm.seedtoplate.dto.AuditEventResponse;
import com.farm.seedtoplate.repository.AuditEventRepository;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Service;

@Service
public class AuditService {

    private final AuditEventRepository auditEventRepository;

    public AuditService(AuditEventRepository auditEventRepository) {
        this.auditEventRepository = auditEventRepository;
    }

    public void record(String entityType, UUID entityId, AuditEventType eventType, String message) {
        AuditEvent event = new AuditEvent();
        event.setEntityType(entityType);
        event.setEntityId(entityId);
        event.setEventType(eventType);
        event.setMessage(message);
        auditEventRepository.save(event);
    }

    public List<AuditEventResponse> listEvents() {
        return auditEventRepository.findAll().stream()
            .map(event -> new AuditEventResponse(
                event.getId(),
                event.getEntityType(),
                event.getEntityId(),
                event.getEventType(),
                event.getMessage(),
                event.getCreatedAt()
            ))
            .toList();
    }
}
