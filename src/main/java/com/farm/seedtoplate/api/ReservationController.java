package com.farm.seedtoplate.api;

import com.farm.seedtoplate.dto.ReservationCreateRequest;
import com.farm.seedtoplate.dto.ReservationResponse;
import com.farm.seedtoplate.security.OtpUserDetails;
import com.farm.seedtoplate.service.ReservationService;
import jakarta.validation.Valid;
import java.util.UUID;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.security.core.annotation.AuthenticationPrincipal;

@RestController
@RequestMapping("/api")
public class ReservationController {

    private final ReservationService reservationService;

    public ReservationController(ReservationService reservationService) {
        this.reservationService = reservationService;
    }

    @PostMapping("/reservations")
    public ResponseEntity<ReservationResponse> createReservation(
        @AuthenticationPrincipal OtpUserDetails user,
        @Valid @RequestBody ReservationCreateRequest request
    ) {
        if (user == null) {
            throw new AccessDeniedException("Not authenticated");
        }
        return ResponseEntity.ok(reservationService.createReservation(user.getId(), request));
    }

    @GetMapping("/reservations/user/{userId}")
    public ResponseEntity<java.util.List<ReservationResponse>> getReservationsForUser(
        @AuthenticationPrincipal OtpUserDetails user,
        @PathVariable UUID userId
    ) {
        if (user == null) {
            throw new AccessDeniedException("Not authenticated");
        }
        if (!user.getId().equals(userId)) {
            throw new AccessDeniedException("You can only read your own reservations");
        }
        return ResponseEntity.ok(reservationService.getReservationsForUser(userId));
    }

    @GetMapping("/reservations/{reservationId}")
    public ResponseEntity<ReservationResponse> getReservation(
        @AuthenticationPrincipal OtpUserDetails user,
        @PathVariable UUID reservationId
    ) {
        if (user == null) {
            throw new AccessDeniedException("Not authenticated");
        }
        return ResponseEntity.ok(
            reservationService.getReservationFor(reservationId, user.getId(), user.isAdmin())
        );
    }

    @DeleteMapping("/reservations/{reservationId}")
    public ResponseEntity<Void> cancelReservation(
        @AuthenticationPrincipal OtpUserDetails user,
        @PathVariable UUID reservationId
    ) {
        if (user == null) {
            throw new AccessDeniedException("Not authenticated");
        }
        reservationService.cancelReservation(reservationId, user.getId(), user.isAdmin());
        return ResponseEntity.noContent().build();
    }
}
