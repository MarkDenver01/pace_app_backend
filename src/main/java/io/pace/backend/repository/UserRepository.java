package io.pace.backend.repository;

import io.pace.backend.domain.enums.RoleState;
import io.pace.backend.domain.model.entity.User;
import lombok.NonNull;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmail(String email);

    Boolean existsByEmail(String email);

    Boolean existsByEmailAndUniversity_UniversityId(String email, Long universityId);

    Boolean existsByEmailAndSignupMethod(String email, String signupMethod);

    List<User> findAllByRole_RoleState(@NonNull RoleState roleState);

    List<User> findAllByUniversity_UniversityId(@NonNull Long universityId);
    List<User> findAllByUniversity_UniversityIdAndRole_RoleStateIn(
            @NonNull Long universityId, @NonNull List<RoleState> roleStates
    );
}
