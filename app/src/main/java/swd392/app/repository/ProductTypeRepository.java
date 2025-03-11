package swd392.app.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import swd392.app.entity.ProductType;

import java.util.Optional;

public interface ProductTypeRepository extends JpaRepository<ProductType, String> {
    Optional<ProductType> findByProductTypeCode(String productTypeCode);
}
