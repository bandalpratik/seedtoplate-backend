package com.farm.seedtoplate.repository;

import com.farm.seedtoplate.domain.CropBatch;
import com.farm.seedtoplate.domain.CropStage;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.UUID;

public interface CropBatchRepository extends JpaRepository<CropBatch, UUID> {
    List<CropBatch> findByCurrentStageNot(CropStage stage);
}
