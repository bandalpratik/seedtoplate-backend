package com.farm.seedtoplate.domain;

import jakarta.persistence.*;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "crop_batches", indexes = {
    @Index(name = "idx_crop_batches_stage", columnList = "current_stage"),
    @Index(name = "idx_crop_batches_farm", columnList = "farm_name")
})
@Getter
@Setter
@NoArgsConstructor
public class CropBatch {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @NotBlank
    @Column(nullable = false)
    private String cropName;

    @NotBlank
    @Column(nullable = false)
    private String farmName;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "current_stage", nullable = false, length = 30)
    private CropStage currentStage = CropStage.GROWING;

    @NotNull
    @DecimalMin("0.0")
    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal plannedYieldKg = BigDecimal.ZERO;

    @NotNull
    @DecimalMin("0.0")
    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal actualYieldKg = BigDecimal.ZERO;

    @NotNull
    @DecimalMin("0.0")
    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal availableYieldKg = BigDecimal.ZERO;

    @NotNull
    @DecimalMin("0.0")
    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal releasedKg = BigDecimal.ZERO;

    @DecimalMin("0.0")
    @Column(precision = 12, scale = 2)
    private BigDecimal estimatedPriceLowPerKg = BigDecimal.ZERO;

    @DecimalMin("0.0")
    @Column(precision = 12, scale = 2)
    private BigDecimal estimatedPriceHighPerKg = BigDecimal.ZERO;

    @DecimalMin("0.0")
    @Column(precision = 12, scale = 2)
    private BigDecimal finalRetailPricePerKg;

    @Column(length = 500)
    private String description;

    @Column(length = 50)
    private String variety;

    @Column(nullable = false)
    private Instant createdAt = Instant.now();

    @Column(nullable = false)
    private Instant updatedAt = Instant.now();

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
