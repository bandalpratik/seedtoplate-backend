package com.farm.seedtoplate.repository;

import com.farm.seedtoplate.domain.Manifest;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.UUID;

public interface ManifestRepository extends JpaRepository<Manifest, UUID> {
}
