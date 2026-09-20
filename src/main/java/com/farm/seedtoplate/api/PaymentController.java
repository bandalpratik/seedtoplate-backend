package com.farm.seedtoplate.api;

import com.farm.seedtoplate.dto.PaymentCreateRequest;
import com.farm.seedtoplate.dto.PaymentResponse;
import com.farm.seedtoplate.security.OtpUserDetails;
import com.farm.seedtoplate.service.PaymentService;
import jakarta.validation.Valid;
import java.util.UUID;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestHeader;

@RestController
@RequestMapping("/api")
public class PaymentController {

    private final PaymentService paymentService;

    public PaymentController(PaymentService paymentService) {
        this.paymentService = paymentService;
    }

    @PostMapping("/payments/create-intent")
    public ResponseEntity<PaymentResponse> createPaymentIntent(
        @AuthenticationPrincipal OtpUserDetails user,
        @Valid @RequestBody PaymentCreateRequest request
    ) {
        if (user == null) {
            throw new AccessDeniedException("Not authenticated");
        }
        return ResponseEntity.ok(
            paymentService.createPaymentIntent(request.reservationId(), request.returnBaseUrl(), user.getId(), user.isAdmin())
        );
    }

    @PostMapping("/payments/webhook/razorpay")
    public ResponseEntity<String> handleRazorpayWebhook(
        @RequestHeader(name = "X-Razorpay-Signature", required = false) String signature,
        @RequestBody String body
    ) {
        paymentService.handleRazorpayWebhook(body, signature);
        return ResponseEntity.ok("ok");
    }

    /**
     * Settling a payment is what flips a reservation to PAID. It must never be
     * reachable by the customer — only the provider webhook or an operator.
     */
    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/payments/{paymentId}/settle")
    public ResponseEntity<PaymentResponse> settlePayment(@PathVariable UUID paymentId) {
        return ResponseEntity.ok(paymentService.markPaid(paymentId));
    }
}
