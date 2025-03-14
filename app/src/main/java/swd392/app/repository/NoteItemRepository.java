package swd392.app.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import swd392.app.entity.NoteItem;

import java.util.List;
import java.util.Optional;

@Repository
public interface NoteItemRepository extends JpaRepository<NoteItem, String> {

    @Query("SELECT COALESCE(SUM(n.quantity), 0) FROM NoteItem n WHERE n.product.productCode = :productCode AND " +
            "n.exchangeNote.transactionType = 'IMPORT' AND n.exchangeNote.destinationWarehouse.warehouseCode = :warehouseCode")
    Integer getTotalImportByProductCodeAndWarehouse(@Param("productCode") String productCode,
                                                    @Param("warehouseCode") String warehouseCode);
    
    @Query("SELECT COALESCE(SUM(n.quantity), 0) FROM NoteItem n WHERE n.product.productCode = :productCode AND " +
            "n.exchangeNote.transactionType = 'EXPORT' AND n.exchangeNote.sourceWarehouse.warehouseCode = :warehouseCode")
    Integer getTotalExportByProductCodeAndWarehouse(@Param("productCode") String productCode,
                                                    @Param("warehouseCode") String warehouseCode);
}

