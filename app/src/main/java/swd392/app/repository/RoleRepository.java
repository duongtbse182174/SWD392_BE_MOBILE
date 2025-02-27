package swd392.app.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import swd392.app.entity.Role;

import java.util.List;
import java.util.Optional;

public interface RoleRepository extends JpaRepository<Role, String> {
    Optional<Role> findByRoleName(String roleName);
    List<Role> findAll();
}


