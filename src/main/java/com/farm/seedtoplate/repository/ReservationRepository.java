package com.farm.seedtoplate.repository;

import com.farm.seedtoplate.domain.CropBatch;
import com.farm.seedtoplate.domain.Reservation;
import com.farm.seedtoplate.domain.ReservationStatus;
import com.farm.seedtoplate.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.UUID;

public interface ReservationRepository extends JpaRepository<Reservation, UUID> {
    List<Reservation> findByUser(User user);
    List<Reservation> findByUser_Id(UUID userId);
    List<Reservation> findByBatch(CropBatch batch);
    List<Reservation> findByBatchAndStatus(CropBatch batch, ReservationStatus status);
    List<Reservation> findByBatchAndStatusOrderByCreatedAtAsc(CropBatch batch, ReservationStatus status);
    long countByBatchAndStatus(CropBatch batch, ReservationStatus status);
}
