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
@Table(name = "batch_releases", indexes = {
    @Index(name = "idx_batch_releases_batch", columnList = "batch_id"),
    @Index(name = "idx_batch_releases_released_at", columnList = "released_at")
})
@Getter
@Setter
@NoArgsConstructor
public class BatchRelease {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "batch_id", nullable = false)
    private CropBatch batch;

    @NotNull
    @DecimalMin("0.01")
    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal releasedKg;

    @NotNull
    @DecimalMin("0.0")
    @Column(name = "price_per_kg", nullable = false, precision = 12, scale = 2)
    private BigDecimal pricePerKg;

    @Column(name = "released_at", nullable = false)
    private Instant releasedAt = Instant.now();

    @Column(length = 500)
    private String note;
}
