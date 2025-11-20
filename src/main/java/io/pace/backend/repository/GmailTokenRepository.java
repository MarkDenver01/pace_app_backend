package io.pace.backend.repository;

import io.pace.backend.domain.model.entity.GmailToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface GmailTokenRepository extends JpaRepository<GmailToken, Long> {}
