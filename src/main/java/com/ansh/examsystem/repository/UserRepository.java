package com.ansh.examsystem.repository;

import com.ansh.examsystem.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;
import com.ansh.examsystem.model.Role;
import java.util.List;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmail(String email);
    boolean existsByEmail(String email);
    List<User> findByRole(Role role);
    List<User> findByRoleAndSchoolClassId(Role role, Long schoolClassId);
}
