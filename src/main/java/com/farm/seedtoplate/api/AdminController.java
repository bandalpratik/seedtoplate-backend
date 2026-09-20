package com.farm.seedtoplate.api;

import com.farm.seedtoplate.dto.AdminBatchSummaryResponse;
import com.farm.seedtoplate.dto.CropBatchCreateRequest;
import com.farm.seedtoplate.dto.CropBatchResponse;
import com.farm.seedtoplate.dto.ReleasePreviewRequest;
import com.farm.seedtoplate.dto.ReleasePreviewResponse;
import com.farm.seedtoplate.service.AdminService;
import jakarta.validation.Valid;
import java.util.List;
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
public class AdminController {

    private final AdminService adminService;

    public AdminController(AdminService adminService) {
        this.adminService = adminService;
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/batches")
    public ResponseEntity<List<AdminBatchSummaryResponse>> getBatches() {
        return ResponseEntity.ok(adminService.listBatches());
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/batches")
    public ResponseEntity<CropBatchResponse> createBatch(@Valid @RequestBody CropBatchCreateRequest request) {
        return ResponseEntity.ok(adminService.createBatch(request));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/batches/{batchId}/release/preview")
    public ResponseEntity<ReleasePreviewResponse> previewRelease(
        @PathVariable UUID batchId,
        @Valid @RequestBody ReleasePreviewRequest request
    ) {
        return ResponseEntity.ok(adminService.previewRelease(batchId, request));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/batches/{batchId}/release/confirm")
    public ResponseEntity<ReleasePreviewResponse> confirmRelease(
        @PathVariable UUID batchId,
        @Valid @RequestBody ReleasePreviewRequest request
    ) {
        return ResponseEntity.ok(adminService.confirmRelease(batchId, request));
    }
}
