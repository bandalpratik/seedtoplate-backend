package com.farm.seedtoplate.service;

import com.farm.seedtoplate.domain.PaymentIntent;
import com.farm.seedtoplate.domain.PaymentStatus;
import com.farm.seedtoplate.domain.Reservation;
import com.farm.seedtoplate.domain.ReservationStatus;
import com.farm.seedtoplate.domain.FulfillmentStatus;
import com.farm.seedtoplate.dto.PaymentResponse;
import com.farm.seedtoplate.exception.ApiException;
import com.farm.seedtoplate.repository.PaymentIntentRepository;
import com.farm.seedtoplate.repository.ReservationRepository;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Service
public class PaymentService {

    private final PaymentIntentRepository paymentIntentRepository;
    private final ReservationRepository reservationRepository;
    private final AuditService auditService;
    private final RazorpayService razorpayService;

    public PaymentService(
        PaymentIntentRepository paymentIntentRepository,
        ReservationRepository reservationRepository,
        AuditService auditService,
        RazorpayService razorpayService
    ) {
        this.paymentIntentRepository = paymentIntentRepository;
        this.reservationRepository = reservationRepository;
        this.auditService = auditService;
        this.razorpayService = razorpayService;
    }

    @Transactional
    public PaymentResponse createPaymentIntent(UUID reservationId, String returnBaseUrl, UUID requesterId, boolean isAdmin) {
        Reservation reservation = reservationRepository.findById(reservationId)
            .orElseThrow(() -> new ApiException("Reservation not found"));

        if (!isAdmin && (requesterId == null || !reservation.getUser().getId().equals(requesterId))) {
            throw new AccessDeniedException("This reservation belongs to someone else");
        }

        if (reservation.getStatus() == ReservationStatus.CANCELLED) {
            throw new ApiException("Reservation is cancelled");
        }

        if (reservation.getStatus() != ReservationStatus.AWAITING_PAYMENT) {
            throw new ApiException("This reservation is not awaiting payment");
        }

        if (reservation.getPaymentDueAt() != null && Instant.now().isAfter(reservation.getPaymentDueAt())) {
            throw new ApiException("The payment window for this reservation has closed");
        }

        BigDecimal amount = reservation.getPricePerKg() == null
            ? BigDecimal.ZERO
            : reservation.getReservedKg().multiply(reservation.getPricePerKg());

        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new ApiException("Reservation has no release price yet");
        }

        Optional<PaymentIntent> existing = paymentIntentRepository.findByReservation(reservation);
        PaymentIntent paymentIntent = existing.orElseGet(PaymentIntent::new);
        paymentIntent.setReservation(reservation);
        paymentIntent.setAmount(amount);
        paymentIntentRepository.save(paymentIntent);

        if (paymentIntent.getStatus() == PaymentStatus.INITIATED && StringUtils.hasText(paymentIntent.getProviderRef())) {
            PaymentResponse synced = syncPaymentIntent(paymentIntent);
            if (synced.status() == PaymentStatus.INITIATED && StringUtils.hasText(synced.paymentLink())) {
                return synced;
            }
        }

        String callbackUrl = buildCallbackUrl(returnBaseUrl, reservation.getId(), paymentIntent.getId());
        RazorpayService.RazorpayLinkResponse link = razorpayService.createPaymentLink(
            paymentIntent.getId(),
            reservation.getId(),
            amount,
            reservation.getPaymentDueAt(),
            callbackUrl
        );
        paymentIntent.setStatus(PaymentStatus.INITIATED);
        paymentIntent.setProviderRef(link.linkId());
        paymentIntentRepository.save(paymentIntent);

        auditService.record("RESERVATION", reservation.getId(), com.farm.seedtoplate.domain.AuditEventType.PAYMENT_CREATED,
            "Payment intent created for ₹" + amount);

        return new PaymentResponse(
            paymentIntent.getId(),
            reservation.getId(),
            paymentIntent.getAmount(),
            link.status(),
            link.linkId(),
            link.shortUrl()
        );
    }

    @Transactional
    public PaymentResponse markPaid(UUID paymentId) {
        PaymentIntent paymentIntent = paymentIntentRepository.findById(paymentId)
            .orElseThrow(() -> new ApiException("Payment not found"));

        paymentIntent.setStatus(PaymentStatus.SUCCESS);
        paymentIntentRepository.save(paymentIntent);

        Reservation reservation = paymentIntent.getReservation();
        reservation.setStatus(ReservationStatus.PAID_READY_FOR_PICKUP);
        reservation.setFulfillmentStatus(FulfillmentStatus.PAID);
        reservation.setPaymentDueAt(null);
        reservationRepository.save(reservation);

        auditService.record("PAYMENT", paymentIntent.getId(), com.farm.seedtoplate.domain.AuditEventType.PAYMENT_SETTLED,
            "Payment settled successfully");

        return new PaymentResponse(
            paymentIntent.getId(),
            reservation.getId(),
            paymentIntent.getAmount(),
            paymentIntent.getStatus(),
            paymentIntent.getProviderRef(),
            "/checkout/" + reservation.getId() + "/return?orderId=" + paymentIntent.getId()
        );
    }

    @Transactional
    public PaymentResponse syncPaymentIntent(UUID paymentId, UUID requesterId, boolean isAdmin) {
        PaymentIntent paymentIntent = paymentIntentRepository.findById(paymentId)
            .orElseThrow(() -> new ApiException("Payment not found"));
        Reservation reservation = paymentIntent.getReservation();
        if (!isAdmin && (requesterId == null || !reservation.getUser().getId().equals(requesterId))) {
            throw new AccessDeniedException("This payment belongs to someone else");
        }
        return syncPaymentIntent(paymentIntent);
    }

    @Transactional
    public void handleRazorpayWebhook(String body, String signature) {
        if (!razorpayService.verifyWebhookSignature(body, signature)) {
            throw new AccessDeniedException("Invalid webhook signature");
        }

        RazorpayService.RazorpayWebhookEvent event = razorpayService.parseWebhook(body);
        if (!StringUtils.hasText(event.linkId())) {
            return;
        }

        PaymentIntent paymentIntent = paymentIntentRepository.findByProviderRef(event.linkId())
            .orElseThrow(() -> new ApiException("Payment not found"));

        if (event.status() == PaymentStatus.SUCCESS) {
            completePayment(paymentIntent, true);
            return;
        }

        if (event.status() == PaymentStatus.FAILED && paymentIntent.getStatus() != PaymentStatus.SUCCESS) {
            paymentIntent.setStatus(PaymentStatus.FAILED);
            paymentIntentRepository.save(paymentIntent);
        }
    }

    private PaymentResponse syncPaymentIntent(PaymentIntent paymentIntent) {
        Reservation reservation = paymentIntent.getReservation();
        if (!StringUtils.hasText(paymentIntent.getProviderRef())) {
            return toResponse(paymentIntent, null);
        }

        RazorpayService.RazorpayLinkResponse link = razorpayService.fetchPaymentLink(paymentIntent.getProviderRef());
        if (link.status() == PaymentStatus.SUCCESS) {
            completePayment(paymentIntent, false);
            return toResponse(paymentIntent, link.shortUrl());
        }
        if (link.status() == PaymentStatus.FAILED && paymentIntent.getStatus() != PaymentStatus.SUCCESS) {
            paymentIntent.setStatus(PaymentStatus.FAILED);
            paymentIntentRepository.save(paymentIntent);
        }
        return new PaymentResponse(
            paymentIntent.getId(),
            reservation.getId(),
            paymentIntent.getAmount(),
            paymentIntent.getStatus(),
            paymentIntent.getProviderRef(),
            link.shortUrl()
        );
    }

    private void completePayment(PaymentIntent paymentIntent, boolean fromWebhook) {
        if (paymentIntent.getStatus() == PaymentStatus.SUCCESS) {
            return;
        }

        paymentIntent.setStatus(PaymentStatus.SUCCESS);
        paymentIntentRepository.save(paymentIntent);

        Reservation reservation = paymentIntent.getReservation();
        reservation.setStatus(ReservationStatus.PAID_READY_FOR_PICKUP);
        reservation.setFulfillmentStatus(FulfillmentStatus.PAID);
        reservation.setPaymentDueAt(null);
        reservationRepository.save(reservation);

        auditService.record(
            "PAYMENT",
            paymentIntent.getId(),
            com.farm.seedtoplate.domain.AuditEventType.PAYMENT_SETTLED,
            fromWebhook ? "Payment confirmed by Razorpay webhook" : "Payment confirmed by Razorpay status sync"
        );
    }

    private PaymentResponse toResponse(PaymentIntent paymentIntent, String paymentLink) {
        return new PaymentResponse(
            paymentIntent.getId(),
            paymentIntent.getReservation().getId(),
            paymentIntent.getAmount(),
            paymentIntent.getStatus(),
            paymentIntent.getProviderRef(),
            paymentLink
        );
    }

    private String buildCallbackUrl(String returnBaseUrl, UUID reservationId, UUID paymentIntentId) {
        if (!StringUtils.hasText(returnBaseUrl)) {
            throw new ApiException("Frontend return URL is required for payment redirection");
        }
        return returnBaseUrl.replaceAll("/$", "")
            + "/checkout/" + reservationId + "/return?orderId=" + paymentIntentId;
    }
}
