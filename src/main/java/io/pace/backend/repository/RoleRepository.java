package io.pace.backend.repository;

import io.pace.backend.domain.enums.RoleState;
import io.pace.backend.domain.model.entity.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface RoleRepository extends JpaRepository<Role, Long> {
    Optional<Role> findRoleByRoleState(RoleState roleState);
}
