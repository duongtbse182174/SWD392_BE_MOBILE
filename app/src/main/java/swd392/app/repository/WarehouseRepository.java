package swd392.app.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import swd392.app.entity.Role;
import swd392.app.entity.Warehouse;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface WarehouseRepository extends JpaRepository<Warehouse, String> {
    Optional<Warehouse> findByWarehouseCode(String warehouseCode);
    List<Warehouse> findAll();
}
