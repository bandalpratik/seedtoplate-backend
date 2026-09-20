package com.farm.seedtoplate.api;

import com.farm.seedtoplate.domain.ManifestStatus;
import com.farm.seedtoplate.dto.ManifestCreateRequest;
import com.farm.seedtoplate.dto.ManifestResponse;
import com.farm.seedtoplate.service.ManifestService;
import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin")
public class ManifestController {

    private final ManifestService manifestService;

    public ManifestController(ManifestService manifestService) {
        this.manifestService = manifestService;
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/manifest")
    public ResponseEntity<List<ManifestResponse>> listManifests() {
        return ResponseEntity.ok(manifestService.listManifests());
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/manifest")
    public ResponseEntity<ManifestResponse> createManifest(@Valid @RequestBody ManifestCreateRequest request) {
        return ResponseEntity.ok(manifestService.createManifest(request));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/manifest/{manifestId}/status/{status}")
    public ResponseEntity<ManifestResponse> updateStatus(
        @PathVariable UUID manifestId,
        @PathVariable ManifestStatus status
    ) {
        return ResponseEntity.ok(manifestService.updateStatus(manifestId, status));
    }
}
