package swd392.app.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import swd392.app.entity.Product;
import swd392.app.entity.StockCheckProduct;
import swd392.app.entity.Warehouse;

import java.util.Optional;

public interface StockCheckProductRepository extends JpaRepository<StockCheckProduct, String> {
    Optional<StockCheckProduct> findTopByProductAndStockCheckNoteWarehouseOrderByStockCheckNoteDateDesc(Product product, Warehouse warehouse);
}