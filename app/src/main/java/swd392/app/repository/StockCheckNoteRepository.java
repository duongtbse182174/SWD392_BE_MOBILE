package swd392.app.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import swd392.app.entity.StockCheckNote;
import swd392.app.enums.StockCheckStatus;

import java.util.List;

@Repository
public interface StockCheckNoteRepository extends JpaRepository<StockCheckNote, String> {
    List<StockCheckNote> findByWarehouse_WarehouseCode(String warehouseCode);
    List<StockCheckNote> findByStockCheckStatus(StockCheckStatus status);
}
