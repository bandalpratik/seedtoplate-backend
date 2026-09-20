package com.farm.seedtoplate.repository;

import com.farm.seedtoplate.domain.BatchRelease;
import com.farm.seedtoplate.domain.CropBatch;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.UUID;

public interface BatchReleaseRepository extends JpaRepository<BatchRelease, UUID> {
    List<BatchRelease> findByBatchOrderByReleasedAtAsc(CropBatch batch);
}
