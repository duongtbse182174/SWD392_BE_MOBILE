
package swd392.app.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import swd392.app.entity.Product;
import swd392.app.entity.StockCheckProduct;
import swd392.app.entity.Warehouse;
import swd392.app.enums.StockCheckStatus;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface StockCheckProductRepository extends JpaRepository<StockCheckProduct, UUID> {

    @Query("SELECT scp FROM StockCheckProduct scp " +
            "JOIN scp.stockCheckNote scn " +
            "JOIN scp.product p " +
            "JOIN scn.warehouse w " +
            "WHERE p.productCode = :productCode " +
            "AND w.warehouseCode = :warehouseCode " +
            "ORDER BY scn.date DESC LIMIT 1")
    Optional<StockCheckProduct> findLatestStockCheck(
            @Param("productCode") String productCode,
            @Param("warehouseCode") String warehouseCode
    );
}
