package com.farm.seedtoplate.service;

import com.farm.seedtoplate.domain.CropBatch;
import com.farm.seedtoplate.domain.Manifest;
import com.farm.seedtoplate.domain.ManifestStatus;
import com.farm.seedtoplate.dto.ManifestCreateRequest;
import com.farm.seedtoplate.dto.ManifestResponse;
import com.farm.seedtoplate.exception.ApiException;
import com.farm.seedtoplate.repository.CropBatchRepository;
import com.farm.seedtoplate.repository.ManifestRepository;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Service;

@Service
public class ManifestService {

    private final ManifestRepository manifestRepository;
    private final CropBatchRepository cropBatchRepository;

    public ManifestService(ManifestRepository manifestRepository, CropBatchRepository cropBatchRepository) {
        this.manifestRepository = manifestRepository;
        this.cropBatchRepository = cropBatchRepository;
    }

    public List<ManifestResponse> listManifests() {
        return manifestRepository.findAll().stream()
            .map(this::toDto)
            .toList();
    }

    public ManifestResponse createManifest(ManifestCreateRequest request) {
        CropBatch batch = cropBatchRepository.findById(request.batchId())
            .orElseThrow(() -> new ApiException("Batch not found"));

        Manifest manifest = new Manifest();
        manifest.setBatch(batch);
        manifest.setZone(request.zone());
        manifest.setRoute(request.route());
        manifest.setLoadKg(request.loadKg());
        manifest.setHandoffStatus(ManifestStatus.PLANNED);
        manifestRepository.save(manifest);

        return toDto(manifest);
    }

    public ManifestResponse updateStatus(UUID manifestId, ManifestStatus status) {
        Manifest manifest = manifestRepository.findById(manifestId)
            .orElseThrow(() -> new ApiException("Manifest not found"));
        manifest.setHandoffStatus(status);
        manifestRepository.save(manifest);
        return toDto(manifest);
    }

    private ManifestResponse toDto(Manifest manifest) {
        return new ManifestResponse(
            manifest.getId(),
            manifest.getBatch().getId(),
            manifest.getZone(),
            manifest.getRoute(),
            manifest.getLoadKg(),
            manifest.getHandoffStatus().name(),
            manifest.getCreatedAt()
        );
    }
}
