package io.pace.backend.repository;

import io.pace.backend.domain.model.entity.University;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UniversityRepository extends JpaRepository<University, Integer> {
    Optional<University> findByUniversityName(String universityName);

    boolean existsUniversityByUniversityId(Long universityId);

    boolean existsByUniversityName(String universityName);
}
