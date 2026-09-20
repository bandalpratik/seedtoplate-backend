package com.farm.seedtoplate.domain;

import jakarta.persistence.*;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "reservations", indexes = {
    @Index(name = "idx_reservations_user", columnList = "user_id"),
    @Index(name = "idx_reservations_batch", columnList = "batch_id"),
    @Index(name = "idx_reservations_status", columnList = "status")
})
@Getter
@Setter
@NoArgsConstructor
public class Reservation {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "batch_id", nullable = false)
    private CropBatch batch;

    @NotNull
    @DecimalMin("0.01")
    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal reservedKg;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private ReservationStatus status = ReservationStatus.PENDING_RELEASE;

    @Column(nullable = false)
    private Instant createdAt = Instant.now();

    @Column(nullable = false)
    private Instant updatedAt = Instant.now();

    @Column(precision = 12, scale = 2)
    private BigDecimal pricePerKg;

    @Column(length = 40)
    private String pickupLocation;

    @Enumerated(EnumType.STRING)
    @Column(name = "fulfillment_status", nullable = false, length = 30)
    private FulfillmentStatus fulfillmentStatus = FulfillmentStatus.PAID;

    @Column
    private Instant paymentDueAt;

    @Column(precision = 12, scale = 2)
    private BigDecimal expectedPriceLowPerKg;

    @Column(precision = 12, scale = 2)
    private BigDecimal expectedPriceHighPerKg;

    @Column(length = 200)
    private String cancelReason;

    @Column
    private UUID releaseId;

    @PrePersist
    void prePersist() {
        if (createdAt == null) {
            createdAt = Instant.now();
        }
        updatedAt = Instant.now();
    }

    @PreUpdate
    void preUpdate() {
        updatedAt = Instant.now();
    }
}
