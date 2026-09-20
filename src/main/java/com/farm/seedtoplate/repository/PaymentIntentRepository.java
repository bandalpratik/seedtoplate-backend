package com.farm.seedtoplate.repository;

import com.farm.seedtoplate.domain.PaymentIntent;
import com.farm.seedtoplate.domain.Reservation;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;
import java.util.UUID;

public interface PaymentIntentRepository extends JpaRepository<PaymentIntent, UUID> {
    Optional<PaymentIntent> findByReservation(Reservation reservation);
    Optional<PaymentIntent> findByProviderRef(String providerRef);
}
