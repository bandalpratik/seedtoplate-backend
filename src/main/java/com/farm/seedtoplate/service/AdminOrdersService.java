package com.farm.seedtoplate.service;

import com.farm.seedtoplate.domain.FulfillmentStatus;
import com.farm.seedtoplate.domain.PaymentIntent;
import com.farm.seedtoplate.domain.PaymentStatus;
import com.farm.seedtoplate.domain.Reservation;
import com.farm.seedtoplate.domain.ReservationStatus;
import com.farm.seedtoplate.dto.AdminOrderResponse;
import com.farm.seedtoplate.exception.ApiException;
import com.farm.seedtoplate.repository.PaymentIntentRepository;
import com.farm.seedtoplate.repository.ReservationRepository;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AdminOrdersService {

    private final ReservationRepository reservationRepository;
    private final PaymentIntentRepository paymentIntentRepository;

    public AdminOrdersService(ReservationRepository reservationRepository, PaymentIntentRepository paymentIntentRepository) {
        this.reservationRepository = reservationRepository;
        this.paymentIntentRepository = paymentIntentRepository;
    }

    @Transactional(readOnly = true)
    public List<AdminOrderResponse> listOrders() {
        return reservationRepository.findAll().stream()
            .filter(r ->
                r.getStatus() == ReservationStatus.AWAITING_PAYMENT
                    || r.getStatus() == ReservationStatus.PAID_READY_FOR_PICKUP
                    || r.getStatus() == ReservationStatus.COMPLETED
            )
            .sorted((a, b) -> b.getCreatedAt().compareTo(a.getCreatedAt()))
            .map(this::toResponse)
            .toList();
    }

    @Transactional
    public AdminOrderResponse updateStatus(UUID reservationId, FulfillmentStatus nextStatus) {
        Reservation reservation = reservationRepository.findById(reservationId)
            .orElseThrow(() -> new ApiException("Reservation not found"));
        PaymentStatus paymentStatus = paymentIntentRepository.findByReservation(reservation)
            .map(PaymentIntent::getStatus)
            .orElse(PaymentStatus.PENDING);

        if (reservation.getStatus() == ReservationStatus.CANCELLED) {
            throw new ApiException("Cancelled reservations cannot move through fulfillment");
        }
        if (paymentStatus != PaymentStatus.SUCCESS) {
            throw new ApiException("Order cannot move through fulfillment until payment is complete");
        }

        if (nextStatus == FulfillmentStatus.DELIVERED) {
            reservation.setStatus(ReservationStatus.COMPLETED);
            reservation.setFulfillmentStatus(FulfillmentStatus.DELIVERED);
        } else {
            reservation.setStatus(ReservationStatus.PAID_READY_FOR_PICKUP);
            reservation.setFulfillmentStatus(nextStatus);
        }

        reservationRepository.save(reservation);
        return toResponse(reservation);
    }

    private AdminOrderResponse toResponse(Reservation reservation) {
        PaymentIntent paymentIntent = paymentIntentRepository.findByReservation(reservation).orElse(null);
        PaymentStatus paymentStatus = paymentIntent != null ? paymentIntent.getStatus() : PaymentStatus.PENDING;
        BigDecimal quantity = reservation.getReservedKg();
        BigDecimal totalAmount = reservation.getPricePerKg() == null
            ? BigDecimal.ZERO
            : reservation.getReservedKg().multiply(reservation.getPricePerKg());

        return new AdminOrderResponse(
            reservation.getId(),
            reservation.getId(),
            reservation.getUser().getId(),
            reservation.getUser().getFullName(),
            reservation.getUser().getPhone(),
            reservation.getBatch().getCropName(),
            reservation.getBatch().getVariety(),
            quantity,
            reservation.getPickupLocation(),
            reservation.getPricePerKg(),
            totalAmount,
            paymentStatus,
            reservation.getFulfillmentStatus() == null ? FulfillmentStatus.PAID : reservation.getFulfillmentStatus(),
            reservation.getCreatedAt(),
            paymentStatus == PaymentStatus.SUCCESS && paymentIntent != null ? paymentIntent.getUpdatedAt() : null
        );
    }
}
