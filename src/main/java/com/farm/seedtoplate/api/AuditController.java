package com.farm.seedtoplate.api;

import com.farm.seedtoplate.dto.AuditEventResponse;
import com.farm.seedtoplate.service.AuditService;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin")
public class AuditController {

    private final AuditService auditService;

    public AuditController(AuditService auditService) {
        this.auditService = auditService;
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/audit")
    public ResponseEntity<List<AuditEventResponse>> listAuditEvents() {
        return ResponseEntity.ok(auditService.listEvents());
    }
}
