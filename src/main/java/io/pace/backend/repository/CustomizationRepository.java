package io.pace.backend.repository;

import io.pace.backend.domain.model.entity.Customization;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CustomizationRepository extends JpaRepository<Customization, Long> {
}
