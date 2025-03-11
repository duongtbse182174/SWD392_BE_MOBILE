package swd392.app.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import swd392.app.entity.InvalidatedToken;


@Repository
public interface InvalidatedTokenRepository extends JpaRepository<InvalidatedToken, String> {}
