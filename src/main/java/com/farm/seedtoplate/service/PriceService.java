package com.farm.seedtoplate.service;

import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;

@Service
public class PriceService {

    public static final BigDecimal SHRINKAGE_RATE = new BigDecimal("0.12");
    public static final BigDecimal STORAGE_CARRY_PER_KG_WEEK = new BigDecimal("1.50");
    public static final BigDecimal PACK_COST = new BigDecimal("4.00");
    public static final BigDecimal LOGISTICS_COST = new BigDecimal("6.00");
    public static final BigDecimal PREMIUM_MULTIPLIER = new BigDecimal("2.00");

    public BigDecimal suggestedPricePerKg(BigDecimal mandiRate) {
        BigDecimal shrinkageMultiplier = BigDecimal.ONE.divide(BigDecimal.ONE.subtract(SHRINKAGE_RATE), 6, RoundingMode.HALF_UP);
        BigDecimal base = mandiRate.multiply(shrinkageMultiplier).multiply(PREMIUM_MULTIPLIER);
        return base.add(STORAGE_CARRY_PER_KG_WEEK).add(PACK_COST).add(LOGISTICS_COST).setScale(2, RoundingMode.HALF_UP);
    }

    public BigDecimal priceBandLow(BigDecimal pricePerKg) {
        return pricePerKg.multiply(new BigDecimal("0.92")).setScale(2, RoundingMode.HALF_UP);
    }

    public BigDecimal priceBandHigh(BigDecimal pricePerKg) {
        return pricePerKg.multiply(new BigDecimal("1.08")).setScale(2, RoundingMode.HALF_UP);
    }

    public BigDecimal marginPercent(BigDecimal pricePerKg, BigDecimal mandiRate) {
        if (mandiRate == null || mandiRate.compareTo(BigDecimal.ZERO) == 0) {
            return BigDecimal.ZERO;
        }
        return pricePerKg.subtract(mandiRate)
            .divide(mandiRate, 4, RoundingMode.HALF_UP)
            .multiply(new BigDecimal("100"))
            .setScale(2, RoundingMode.HALF_UP);
    }
}
