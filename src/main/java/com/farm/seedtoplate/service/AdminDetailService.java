package com.farm.seedtoplate.service;

import com.farm.seedtoplate.domain.AuditEventType;
import com.farm.seedtoplate.domain.BatchRelease;
import com.farm.seedtoplate.domain.CropBatch;
import com.farm.seedtoplate.domain.Reservation;
import com.farm.seedtoplate.domain.ReservationStatus;
import com.farm.seedtoplate.dto.AdminBatchDetailResponse;
import com.farm.seedtoplate.dto.BatchReleaseResponse;
import com.farm.seedtoplate.dto.TimelineEventRequest;
import com.farm.seedtoplate.exception.ApiException;
import com.farm.seedtoplate.repository.BatchReleaseRepository;
import com.farm.seedtoplate.repository.CropBatchRepository;
import com.farm.seedtoplate.repository.ReservationRepository;
import com.farm.seedtoplate.service.AuditService;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AdminDetailService {

    private final CropBatchRepository cropBatchRepository;
    private final BatchReleaseRepository batchReleaseRepository;
    private final ReservationRepository reservationRepository;
    private final AuditService auditService;

    public AdminDetailService(
        CropBatchRepository cropBatchRepository,
        BatchReleaseRepository batchReleaseRepository,
        ReservationRepository reservationRepository,
        AuditService auditService
    ) {
        this.cropBatchRepository = cropBatchRepository;
        this.batchReleaseRepository = batchReleaseRepository;
        this.reservationRepository = reservationRepository;
        this.auditService = auditService;
    }

    public AdminBatchDetailResponse getBatchDetail(UUID batchId) {
        CropBatch batch = cropBatchRepository.findById(batchId)
            .orElseThrow(() -> new ApiException("Batch not found"));

        List<BatchReleaseResponse> releases = batchReleaseRepository.findByBatchOrderByReleasedAtAsc(batch).stream()
            .map(release -> new BatchReleaseResponse(
                release.getId(),
                release.getBatch().getId(),
                release.getReleasedKg(),
                release.getPricePerKg(),
                release.getReleasedAt(),
                release.getNote()
            ))
            .toList();

        return new AdminBatchDetailResponse(
            batch.getId(),
            batch.getCropName(),
            batch.getFarmName(),
            batch.getCurrentStage(),
            batch.getPlannedYieldKg(),
            batch.getActualYieldKg(),
            batch.getAvailableYieldKg(),
            batch.getReleasedKg(),
            batch.getEstimatedPriceLowPerKg(),
            batch.getEstimatedPriceHighPerKg(),
            batch.getFinalRetailPricePerKg(),
            batch.getDescription(),
            batch.getVariety(),
            releases,
            reservationRepository.countByBatchAndStatus(batch, ReservationStatus.PENDING_RELEASE)
        );
    }

    @Transactional
    public AdminBatchDetailResponse addTimelineEvent(UUID batchId, TimelineEventRequest request) {
        CropBatch batch = cropBatchRepository.findById(batchId)
            .orElseThrow(() -> new ApiException("Batch not found"));

        batch.setCurrentStage(request.stage());
        if (request.actualYieldKg() != null) {
            java.math.BigDecimal previousCapacity = batch.getActualYieldKg() != null
                && batch.getActualYieldKg().compareTo(java.math.BigDecimal.ZERO) > 0
                ? batch.getActualYieldKg()
                : batch.getPlannedYieldKg();
            java.math.BigDecimal committedKg = reservationRepository.findByBatch(batch).stream()
                .filter(reservation -> reservation.getStatus() != ReservationStatus.CANCELLED)
                .map(Reservation::getReservedKg)
                .reduce(java.math.BigDecimal.ZERO, java.math.BigDecimal::add);
            batch.setActualYieldKg(request.actualYieldKg());
            java.math.BigDecimal correctedAvailable = request.actualYieldKg().subtract(committedKg);
            batch.setAvailableYieldKg(correctedAvailable.max(java.math.BigDecimal.ZERO));
            if (batch.getPlannedYieldKg() == null || batch.getPlannedYieldKg().compareTo(java.math.BigDecimal.ZERO) <= 0) {
                batch.setPlannedYieldKg(previousCapacity);
            }
        }
        cropBatchRepository.save(batch);
        auditService.record(
            "BATCH",
            batch.getId(),
            request.actualYieldKg() != null ? AuditEventType.WEIGH_IN_CAPTURED : AuditEventType.STAGE_UPDATED,
            request.title() + (request.description() == null ? "" : " · " + request.description())
        );

        return getBatchDetail(batchId);
    }
}
