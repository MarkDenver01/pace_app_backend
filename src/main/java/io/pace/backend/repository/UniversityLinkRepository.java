package io.pace.backend.repository;

import io.pace.backend.domain.model.entity.UniversityLink;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UniversityLinkRepository extends JpaRepository<UniversityLink, Long> {
    Optional<UniversityLink> findByUniversityUniversityId(Long universityUniversityId);

    Optional<UniversityLink> findByUniversityUniversityIdAndToken(Long universityId, String token);

    Optional<UniversityLink> findByToken(String token);
    UniversityLink findByUniversity_UniversityId(Long universityId);
}
