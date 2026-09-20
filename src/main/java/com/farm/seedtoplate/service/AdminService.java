package com.farm.seedtoplate.service;

import com.farm.seedtoplate.domain.AuditEventType;
import com.farm.seedtoplate.domain.BatchRelease;
import com.farm.seedtoplate.domain.CropBatch;
import com.farm.seedtoplate.domain.CropStage;
import com.farm.seedtoplate.domain.Reservation;
import com.farm.seedtoplate.domain.ReservationStatus;
import com.farm.seedtoplate.dto.AdminBatchSummaryResponse;
import com.farm.seedtoplate.dto.CropBatchCreateRequest;
import com.farm.seedtoplate.dto.CropBatchResponse;
import com.farm.seedtoplate.dto.ReleasePreviewRequest;
import com.farm.seedtoplate.dto.ReleasePreviewResponse;
import com.farm.seedtoplate.exception.ApiException;
import com.farm.seedtoplate.repository.BatchReleaseRepository;
import com.farm.seedtoplate.repository.CropBatchRepository;
import com.farm.seedtoplate.repository.ReservationRepository;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AdminService {

    private final CropBatchRepository cropBatchRepository;
    private final ReservationRepository reservationRepository;
    private final BatchReleaseRepository batchReleaseRepository;
    private final PriceService priceService;
    private final AuditService auditService;

    public AdminService(
        CropBatchRepository cropBatchRepository,
        ReservationRepository reservationRepository,
        BatchReleaseRepository batchReleaseRepository,
        PriceService priceService,
        AuditService auditService
    ) {
        this.cropBatchRepository = cropBatchRepository;
        this.reservationRepository = reservationRepository;
        this.batchReleaseRepository = batchReleaseRepository;
        this.priceService = priceService;
        this.auditService = auditService;
    }

    public List<AdminBatchSummaryResponse> listBatches() {
        return cropBatchRepository.findAll().stream()
            .map(batch -> new AdminBatchSummaryResponse(
                batch.getId(),
                batch.getCropName(),
                batch.getFarmName(),
                batch.getVariety(),
                batch.getCurrentStage(),
                batch.getPlannedYieldKg(),
                batch.getActualYieldKg(),
                batch.getAvailableYieldKg(),
                batch.getReleasedKg(),
                batch.getEstimatedPriceLowPerKg(),
                batch.getEstimatedPriceHighPerKg(),
                batch.getFinalRetailPricePerKg(),
                reservationRepository.countByBatchAndStatus(batch, ReservationStatus.PENDING_RELEASE),
                reservationRepository.findByBatchAndStatus(batch, ReservationStatus.PENDING_RELEASE).stream()
                    .map(Reservation::getReservedKg)
                    .reduce(BigDecimal.ZERO, BigDecimal::add)
            ))
            .toList();
    }

    public CropBatchResponse createBatch(CropBatchCreateRequest request) {
        CropStage initialStage = request.currentStage() != null
            ? request.currentStage()
            : request.finalRetailPricePerKg() != null ? CropStage.STORED_CURING : CropStage.GROWING;
        boolean earlyStage = initialStage == CropStage.SOWN || initialStage == CropStage.GROWING;
        BigDecimal plannedYield = request.plannedYieldKg() == null ? BigDecimal.ZERO : request.plannedYieldKg();
        BigDecimal actualYield = request.actualYieldKg() == null ? BigDecimal.ZERO : request.actualYieldKg();
        validateBatchPricing(request);
        if (earlyStage && plannedYield.compareTo(BigDecimal.ZERO) <= 0) {
            throw new ApiException("Enter the expected sellable yield for a crop that is not yet in hand");
        }
        if (!earlyStage && actualYield.compareTo(BigDecimal.ZERO) <= 0) {
            throw new ApiException("Enter the stock currently in hand for harvested or stored batches");
        }
        if (plannedYield.compareTo(BigDecimal.ZERO) <= 0) {
            plannedYield = actualYield;
        }
        CropBatch batch = new CropBatch();
        batch.setCropName(request.cropName().trim());
        batch.setFarmName(request.farmName().trim());
        batch.setVariety(request.variety() == null || request.variety().isBlank() ? null : request.variety().trim());
        batch.setDescription(request.description() == null || request.description().isBlank() ? null : request.description().trim());
        batch.setCurrentStage(initialStage);
        batch.setPlannedYieldKg(plannedYield);
        batch.setActualYieldKg(actualYield);
        batch.setAvailableYieldKg(earlyStage ? plannedYield : actualYield);
        batch.setReleasedKg(BigDecimal.ZERO);
        batch.setEstimatedPriceLowPerKg(request.estimatedPriceLowPerKg());
        batch.setEstimatedPriceHighPerKg(request.estimatedPriceHighPerKg());
        batch.setFinalRetailPricePerKg(request.finalRetailPricePerKg());

        CropBatch saved = cropBatchRepository.save(batch);
        return new CropBatchResponse(
            saved.getId(),
            saved.getCropName(),
            saved.getFarmName(),
            saved.getCurrentStage(),
            saved.getDescription(),
            saved.getVariety(),
            true,
            saved.getPlannedYieldKg(),
            saved.getActualYieldKg(),
            saved.getAvailableYieldKg(),
            saved.getReleasedKg(),
            saved.getEstimatedPriceLowPerKg(),
            saved.getEstimatedPriceHighPerKg(),
            saved.getFinalRetailPricePerKg()
        );
    }

    private void validateBatchPricing(CropBatchCreateRequest request) {
        BigDecimal low = request.estimatedPriceLowPerKg();
        BigDecimal high = request.estimatedPriceHighPerKg();
        BigDecimal fixed = request.finalRetailPricePerKg();
        CropStage stage = request.currentStage() == null ? CropStage.GROWING : request.currentStage();
        boolean earlyStage = stage == CropStage.SOWN || stage == CropStage.GROWING;

        if (fixed == null && low == null && high == null) {
            throw new ApiException("Enter either a fixed selling price or an expected price band");
        }
        if (earlyStage && fixed != null) {
            throw new ApiException("A sown or growing crop cannot use fixed-price ready stock mode");
        }
        if (fixed != null && (low != null || high != null)) {
            throw new ApiException("Choose either a fixed price or an expected band, not both");
        }
        if ((low == null) != (high == null)) {
            throw new ApiException("Enter both low and high band prices together");
        }
        if (low != null && high != null && low.compareTo(high) > 0) {
            throw new ApiException("Low band cannot be higher than high band");
        }
    }

    public ReleasePreviewResponse previewRelease(UUID batchId, ReleasePreviewRequest request) {
        CropBatch batch = cropBatchRepository.findById(batchId)
            .orElseThrow(() -> new ApiException("Batch not found"));

        if (batch.getFinalRetailPricePerKg() != null && batch.getFinalRetailPricePerKg().compareTo(BigDecimal.ZERO) > 0) {
            throw new ApiException("This batch already has a fixed price and does not use release slices");
        }

        if (request.releasedKg().compareTo(BigDecimal.ZERO) <= 0) {
            throw new ApiException("Released kg must be greater than zero");
        }

        List<Reservation> pendingReservations = reservationRepository.findByBatchAndStatusOrderByCreatedAtAsc(
            batch,
            ReservationStatus.PENDING_RELEASE
        );

        BigDecimal queuedKg = totalKg(pendingReservations);
        List<Reservation> selectedReservations = collectReservationsForRelease(pendingReservations, request.releasedKg());
        BigDecimal actualReleasedKg = totalKg(selectedReservations);
        List<UUID> reservationIds = selectedReservations.stream().map(Reservation::getId).toList();

        boolean bandBreach = isBandBreach(batch, request.pricePerKg());

        String message;
        if (bandBreach) {
            message = "Price is above the quoted ceiling. Releasing will cancel the affected reservations free of charge.";
        } else if (pendingReservations.isEmpty()) {
            message = "Nobody is in the queue for this batch yet, so there is nothing to release.";
        } else if (selectedReservations.isEmpty()) {
            message = "This slice is smaller than the first reservation in the queue. Release at least "
                + pendingReservations.get(0).getReservedKg() + " kg.";
        } else if (actualReleasedKg.compareTo(request.releasedKg()) < 0) {
            message = "Only " + actualReleasedKg + " kg of the requested " + request.releasedKg()
                + " kg can be allocated. The queue holds " + queuedKg + " kg; the rest stays in store.";
        } else {
            message = "Preview looks valid and remains inside the band.";
        }

        return new ReleasePreviewResponse(
            batch.getId(),
            batch.getAvailableYieldKg(),
            actualReleasedKg,
            request.pricePerKg(),
            priceService.marginPercent(request.pricePerKg(), request.mandiRate()),
            reservationIds,
            bandBreach,
            message
        );
    }

    @Transactional
    public ReleasePreviewResponse confirmRelease(UUID batchId, ReleasePreviewRequest request) {
        CropBatch batch = cropBatchRepository.findById(batchId)
            .orElseThrow(() -> new ApiException("Batch not found"));

        if (batch.getFinalRetailPricePerKg() != null && batch.getFinalRetailPricePerKg().compareTo(BigDecimal.ZERO) > 0) {
            throw new ApiException("This batch already has a fixed price and does not use release slices");
        }

        if (request.releasedKg().compareTo(BigDecimal.ZERO) <= 0) {
            throw new ApiException("Released kg must be greater than zero");
        }

        List<Reservation> pendingReservations = reservationRepository.findByBatchAndStatusOrderByCreatedAtAsc(
            batch,
            ReservationStatus.PENDING_RELEASE
        );

        List<Reservation> selectedReservations = collectReservationsForRelease(pendingReservations, request.releasedKg());
        if (selectedReservations.isEmpty()) {
            throw new ApiException("No pending reservations fit this slice, so there is nothing to release");
        }

        BigDecimal actualReleasedKg = totalKg(selectedReservations);
        List<UUID> reservationIds = selectedReservations.stream().map(Reservation::getId).toList();

        // Band breach: cancel the affected reservations free and hand their kg back to
        // the unreserved pool. This must COMMIT, so it returns a response rather than
        // throwing — an exception here would roll the cancellations straight back.
        if (isBandBreach(batch, request.pricePerKg())) {
            for (Reservation reservation : selectedReservations) {
                reservation.setStatus(ReservationStatus.CANCELLED);
                reservation.setCancelReason("PRICE_ABOVE_BAND");
                reservation.setPaymentDueAt(null);
                batch.setAvailableYieldKg(batch.getAvailableYieldKg().add(reservation.getReservedKg()));
                reservationRepository.save(reservation);
                auditService.record("RESERVATION", reservation.getId(), AuditEventType.RELEASE_CONFIRMED,
                    "Cancelled free — release price ₹" + request.pricePerKg() + "/kg was above the quoted ceiling");
            }
            cropBatchRepository.save(batch);

            return new ReleasePreviewResponse(
                batch.getId(),
                batch.getAvailableYieldKg(),
                BigDecimal.ZERO,
                request.pricePerKg(),
                priceService.marginPercent(request.pricePerKg(), request.mandiRate()),
                reservationIds,
                true,
                reservationIds.size() + " reservation(s) were cancelled free of charge and "
                    + actualReleasedKg + " kg returned to the pool."
            );
        }

        BatchRelease release = new BatchRelease();
        release.setBatch(batch);
        release.setReleasedKg(actualReleasedKg);
        release.setPricePerKg(request.pricePerKg());
        release.setReleasedAt(Instant.now());
        release.setNote(request.note());
        batchReleaseRepository.save(release);
        auditService.record("BATCH", batch.getId(), AuditEventType.RELEASE_CONFIRMED,
            "Released " + actualReleasedKg + " kg at ₹" + request.pricePerKg() + "/kg");

        // These kilos left `availableYieldKg` when they were reserved. Releasing only
        // moves them from the reserved pool into `releasedKg` — debiting available a
        // second time would destroy stock that is still physically in the store.
        batch.setReleasedKg(batch.getReleasedKg().add(actualReleasedKg));
        batch.setCurrentStage(CropStage.BATCH_RELEASED);
        cropBatchRepository.save(batch);

        for (Reservation reservation : selectedReservations) {
            reservation.setStatus(ReservationStatus.AWAITING_PAYMENT);
            reservation.setPricePerKg(request.pricePerKg());
            reservation.setReleaseId(release.getId());
            reservation.setPaymentDueAt(Instant.now().plusSeconds(48 * 60 * 60));
            reservationRepository.save(reservation);
            auditService.record("RESERVATION", reservation.getId(), AuditEventType.RELEASE_CONFIRMED,
                "Reservation moved to AWAITING_PAYMENT");
        }

        return new ReleasePreviewResponse(
            batch.getId(),
            batch.getAvailableYieldKg(),
            actualReleasedKg,
            request.pricePerKg(),
            priceService.marginPercent(request.pricePerKg(), request.mandiRate()),
            reservationIds,
            false,
            "Release confirmed and payment windows have started."
        );
    }

    private boolean isBandBreach(CropBatch batch, BigDecimal pricePerKg) {
        BigDecimal bandHigh = batch.getEstimatedPriceHighPerKg();
        return bandHigh != null
            && bandHigh.compareTo(BigDecimal.ZERO) > 0
            && pricePerKg.compareTo(bandHigh) > 0;
    }

    private BigDecimal totalKg(List<Reservation> reservations) {
        return reservations.stream()
            .map(Reservation::getReservedKg)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private List<Reservation> collectReservationsForRelease(List<Reservation> pendingReservations, BigDecimal targetKg) {
        List<Reservation> selected = new ArrayList<>();
        BigDecimal remaining = targetKg;

        for (Reservation reservation : pendingReservations) {
            if (remaining.compareTo(BigDecimal.ZERO) <= 0) {
                break;
            }

            BigDecimal reservationKg = reservation.getReservedKg();
            if (reservationKg.compareTo(remaining) <= 0) {
                selected.add(reservation);
                remaining = remaining.subtract(reservationKg);
            } else {
                // Stop at strict FIFO boundary; don't overshoot the target or consume extra stock.
                break;
            }
        }

        return selected;
    }
}
