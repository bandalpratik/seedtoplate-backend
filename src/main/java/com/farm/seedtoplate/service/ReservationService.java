package com.farm.seedtoplate.service;

import com.farm.seedtoplate.domain.CropBatch;
import com.farm.seedtoplate.domain.Reservation;
import com.farm.seedtoplate.domain.ReservationStatus;
import com.farm.seedtoplate.domain.User;
import com.farm.seedtoplate.dto.ReservationCreateRequest;
import com.farm.seedtoplate.dto.ReservationResponse;
import com.farm.seedtoplate.exception.ApiException;
import com.farm.seedtoplate.repository.CropBatchRepository;
import com.farm.seedtoplate.repository.ReservationRepository;
import com.farm.seedtoplate.repository.UserRepository;
import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ReservationService {

    private final ReservationRepository reservationRepository;
    private final CropBatchRepository cropBatchRepository;
    private final UserRepository userRepository;

    public ReservationService(
        ReservationRepository reservationRepository,
        CropBatchRepository cropBatchRepository,
        UserRepository userRepository
    ) {
        this.reservationRepository = reservationRepository;
        this.cropBatchRepository = cropBatchRepository;
        this.userRepository = userRepository;
    }

    @Transactional
    public ReservationResponse createReservation(UUID userId, ReservationCreateRequest request) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new ApiException("User not found"));

        CropBatch batch = cropBatchRepository.findById(request.batchId())
            .orElseThrow(() -> new ApiException("Batch not found"));

        if (batch.getAvailableYieldKg().compareTo(request.reservedKg()) < 0) {
            throw new ApiException("Requested quantity exceeds available stock");
        }

        Reservation reservation = new Reservation();
        reservation.setUser(user);
        reservation.setBatch(batch);
        reservation.setReservedKg(request.reservedKg());
        reservation.setPickupLocation(request.pickupLocation());
        reservation.setExpectedPriceLowPerKg(request.expectedPriceLowPerKg());
        reservation.setExpectedPriceHighPerKg(request.expectedPriceHighPerKg());
        BigDecimal fixedPrice = batch.getFinalRetailPricePerKg();
        boolean fixedPriceBatch = fixedPrice != null && fixedPrice.compareTo(BigDecimal.ZERO) > 0;
        reservation.setStatus(fixedPriceBatch ? ReservationStatus.AWAITING_PAYMENT : ReservationStatus.PENDING_RELEASE);
        reservation.setPricePerKg(fixedPriceBatch ? fixedPrice : BigDecimal.ZERO);
        reservation.setPaymentDueAt(
            fixedPriceBatch ? java.time.Instant.now().plusSeconds(48 * 60 * 60) : null
        );

        batch.setAvailableYieldKg(batch.getAvailableYieldKg().subtract(request.reservedKg()));
        reservationRepository.save(reservation);
        cropBatchRepository.save(batch);

        return toResponse(reservation);
    }

    public ReservationResponse getReservation(UUID reservationId) {
        Reservation reservation = reservationRepository.findById(reservationId)
            .orElseThrow(() -> new ApiException("Reservation not found"));
        return toResponse(reservation);
    }

    /**
     * Reservation IDs travel in deep links (the WhatsApp "your harvest is ready"
     * message), so every read and write has to prove the caller owns the row.
     */
    public ReservationResponse getReservationFor(UUID reservationId, UUID requesterId, boolean isAdmin) {
        Reservation reservation = reservationRepository.findById(reservationId)
            .orElseThrow(() -> new ApiException("Reservation not found"));
        assertOwner(reservation, requesterId, isAdmin);
        return toResponse(reservation);
    }

    private void assertOwner(Reservation reservation, UUID requesterId, boolean isAdmin) {
        if (isAdmin) {
            return;
        }
        if (requesterId == null || !reservation.getUser().getId().equals(requesterId)) {
            throw new AccessDeniedException("This reservation belongs to someone else");
        }
    }

    public List<ReservationResponse> getReservationsForUser(UUID userId) {
        return reservationRepository.findByUser_Id(userId).stream()
            .sorted((a, b) -> a.getCreatedAt().compareTo(b.getCreatedAt()))
            .map(this::toResponse)
            .toList();
    }

    @Transactional
    public void cancelReservation(UUID reservationId, UUID requesterId, boolean isAdmin) {
        Reservation reservation = reservationRepository.findById(reservationId)
            .orElseThrow(() -> new ApiException("Reservation not found"));

        assertOwner(reservation, requesterId, isAdmin);

        if (reservation.getStatus() == ReservationStatus.CANCELLED) {
            return;
        }

        if (reservation.getStatus() == ReservationStatus.PAID_READY_FOR_PICKUP
            || reservation.getStatus() == ReservationStatus.COMPLETED) {
            throw new ApiException("This reservation is already paid for and cannot be cancelled here");
        }

        reservation.setStatus(ReservationStatus.CANCELLED);
        reservation.setCancelReason("CUSTOMER_CANCELLED");
        reservation.setPaymentDueAt(null);
        CropBatch batch = reservation.getBatch();
        batch.setAvailableYieldKg(batch.getAvailableYieldKg().add(reservation.getReservedKg()));
        reservationRepository.save(reservation);
        cropBatchRepository.save(batch);
    }

    private ReservationResponse toResponse(Reservation reservation) {
        return new ReservationResponse(
            reservation.getId(),
            reservation.getBatch().getId(),
            reservation.getUser().getId(),
            reservation.getReleaseId(),
            reservation.getReservedKg(),
            reservation.getStatus(),
            reservation.getPricePerKg(),
            reservation.getPickupLocation(),
            reservation.getPaymentDueAt(),
            reservation.getFulfillmentStatus(),
            reservation.getExpectedPriceLowPerKg(),
            reservation.getExpectedPriceHighPerKg(),
            reservation.getCancelReason(),
            reservation.getCreatedAt()
        );
    }
}
