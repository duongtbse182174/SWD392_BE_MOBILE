package swd392.app.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import swd392.app.entity.User;

import java.util.Optional;

@Repository

public interface UserRepository extends JpaRepository<User, String> {

    boolean existsByUserName(String userName);

    boolean existsByEmail(String email);

    boolean existsByUserCode(String userCode);

    Optional<User> findByEmail(String email);

}

