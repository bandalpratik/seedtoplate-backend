package com.farm.seedtoplate.service;

import com.farm.seedtoplate.domain.BatchRelease;
import com.farm.seedtoplate.domain.CropBatch;
import com.farm.seedtoplate.dto.BatchReleaseResponse;
import com.farm.seedtoplate.dto.CropBatchResponse;
import com.farm.seedtoplate.exception.ApiException;
import com.farm.seedtoplate.repository.BatchReleaseRepository;
import com.farm.seedtoplate.repository.CropBatchRepository;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Service;

@Service
public class CropService {

    private final CropBatchRepository cropBatchRepository;
    private final BatchReleaseRepository batchReleaseRepository;

    public CropService(CropBatchRepository cropBatchRepository, BatchReleaseRepository batchReleaseRepository) {
        this.cropBatchRepository = cropBatchRepository;
        this.batchReleaseRepository = batchReleaseRepository;
    }

    public List<CropBatchResponse> getFeed() {
        return cropBatchRepository.findAll().stream()
            .map(this::map)
            .toList();
    }

    public CropBatchResponse getById(UUID batchId) {
        CropBatch batch = cropBatchRepository.findById(batchId)
            .orElseThrow(() -> new ApiException("Batch not found"));
        return map(batch);
    }

    public List<BatchReleaseResponse> getBatchReleases(UUID batchId) {
        CropBatch batch = cropBatchRepository.findById(batchId)
            .orElseThrow(() -> new ApiException("Batch not found"));

        return batchReleaseRepository.findByBatchOrderByReleasedAtAsc(batch).stream()
            .map(this::mapRelease)
            .toList();
    }

    private CropBatchResponse map(CropBatch batch) {
        return new CropBatchResponse(
            batch.getId(),
            batch.getCropName(),
            batch.getFarmName(),
            batch.getCurrentStage(),
            batch.getDescription(),
            batch.getVariety(),
            true,
            batch.getPlannedYieldKg(),
            batch.getActualYieldKg(),
            batch.getAvailableYieldKg(),
            batch.getReleasedKg(),
            batch.getEstimatedPriceLowPerKg(),
            batch.getEstimatedPriceHighPerKg(),
            batch.getFinalRetailPricePerKg()
        );
    }

    private BatchReleaseResponse mapRelease(BatchRelease release) {
        return new BatchReleaseResponse(
            release.getId(),
            release.getBatch().getId(),
            release.getReleasedKg(),
            release.getPricePerKg(),
            release.getReleasedAt(),
            release.getNote()
        );
    }
}
