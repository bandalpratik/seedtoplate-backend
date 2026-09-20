package com.farm.seedtoplate.api;

import com.farm.seedtoplate.domain.FulfillmentStatus;
import com.farm.seedtoplate.dto.AdminOrderResponse;
import com.farm.seedtoplate.service.AdminOrdersService;
import jakarta.validation.Valid;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin")
public class AdminOrdersController {

    private final AdminOrdersService adminOrdersService;

    public AdminOrdersController(AdminOrdersService adminOrdersService) {
        this.adminOrdersService = adminOrdersService;
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/orders")
    public ResponseEntity<List<AdminOrderResponse>> listOrders() {
        return ResponseEntity.ok(adminOrdersService.listOrders());
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PatchMapping("/orders/{orderId}/status")
    public ResponseEntity<AdminOrderResponse> updateStatus(
        @PathVariable UUID orderId,
        @Valid @RequestBody Map<String, String> body
    ) {
        String statusText = body.get("status");
        FulfillmentStatus status = FulfillmentStatus.valueOf(statusText);
        return ResponseEntity.ok(adminOrdersService.updateStatus(orderId, status));
    }
}
