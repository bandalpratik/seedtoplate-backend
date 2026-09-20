package com.farm.seedtoplate.api;

import com.farm.seedtoplate.dto.AdminBatchDetailResponse;
import com.farm.seedtoplate.dto.TimelineEventRequest;
import com.farm.seedtoplate.service.AdminDetailService;
import jakarta.validation.Valid;
import java.util.UUID;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin")
public class AdminDetailController {

    private final AdminDetailService adminDetailService;

    public AdminDetailController(AdminDetailService adminDetailService) {
        this.adminDetailService = adminDetailService;
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/batches/{batchId}")
    public ResponseEntity<AdminBatchDetailResponse> getBatchDetail(@PathVariable UUID batchId) {
        return ResponseEntity.ok(adminDetailService.getBatchDetail(batchId));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/batches/{batchId}/timeline-event")
    public ResponseEntity<AdminBatchDetailResponse> addTimelineEvent(
        @PathVariable UUID batchId,
        @Valid @RequestBody TimelineEventRequest request
    ) {
        return ResponseEntity.ok(adminDetailService.addTimelineEvent(batchId, request));
    }
}
