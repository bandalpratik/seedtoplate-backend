package com.farm.seedtoplate.api;

import com.farm.seedtoplate.dto.CropBatchResponse;
import com.farm.seedtoplate.dto.BatchReleaseResponse;
import com.farm.seedtoplate.service.CropService;
import java.util.List;
import java.util.UUID;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
public class CropController {

    private final CropService cropService;

    public CropController(CropService cropService) {
        this.cropService = cropService;
    }

    @GetMapping({"/crops", "/crops/feed"})
    public ResponseEntity<List<CropBatchResponse>> getFeed() {
        return ResponseEntity.ok(cropService.getFeed());
    }

    @GetMapping("/crops/{batchId}")
    public ResponseEntity<CropBatchResponse> getById(@PathVariable UUID batchId) {
        return ResponseEntity.ok(cropService.getById(batchId));
    }

    @GetMapping("/crops/{batchId}/releases")
    public ResponseEntity<List<BatchReleaseResponse>> getReleases(@PathVariable UUID batchId) {
        return ResponseEntity.ok(cropService.getBatchReleases(batchId));
    }
}
