package com.farm.seedtoplate.api;

import com.farm.seedtoplate.domain.PaymentIntent;
import com.farm.seedtoplate.dto.PaymentResponse;
import com.farm.seedtoplate.dto.PaymentOrderResponse;
import com.farm.seedtoplate.exception.ApiException;
import com.farm.seedtoplate.repository.PaymentIntentRepository;
import com.farm.seedtoplate.security.OtpUserDetails;
import com.farm.seedtoplate.service.PaymentService;
import java.util.UUID;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.transaction.annotation.Transactional;

@RestController
@RequestMapping("/api")
public class PaymentLookupController {

    private final PaymentIntentRepository paymentIntentRepository;
    private final PaymentService paymentService;

    public PaymentLookupController(PaymentIntentRepository paymentIntentRepository, PaymentService paymentService) {
        this.paymentIntentRepository = paymentIntentRepository;
        this.paymentService = paymentService;
    }

    @GetMapping("/payments/order/{paymentId}")
    @Transactional
    public ResponseEntity<PaymentOrderResponse> getPaymentOrder(
        @AuthenticationPrincipal OtpUserDetails user,
        @PathVariable UUID paymentId
    ) {
        if (user == null) {
            throw new AccessDeniedException("Not authenticated");
        }

        PaymentIntent intent = paymentIntentRepository.findById(paymentId)
            .orElseThrow(() -> new ApiException("Payment not found"));

        UUID ownerId = intent.getReservation().getUser().getId();
        if (!user.isAdmin() && !ownerId.equals(user.getId())) {
            throw new AccessDeniedException("This payment belongs to someone else");
        }

        PaymentResponse synced = paymentService.syncPaymentIntent(paymentId, user.getId(), user.isAdmin());
        return ResponseEntity.ok(new PaymentOrderResponse(
            synced.id(),
            synced.reservationId(),
            synced.amount(),
            synced.status(),
            synced.providerRef(),
            synced.paymentLink()
        ));
    }
}
