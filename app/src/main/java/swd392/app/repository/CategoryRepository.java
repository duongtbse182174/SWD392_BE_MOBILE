package swd392.app.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import swd392.app.entity.Category;

import java.util.Optional;

public interface CategoryRepository extends JpaRepository<Category, String> {
    Optional<Category> findByCategoryCode(String categoryCode);
}
