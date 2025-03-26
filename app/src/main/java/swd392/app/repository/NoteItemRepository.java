package swd392.app.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import swd392.app.entity.ExchangeNote;
import swd392.app.entity.NoteItem;
import swd392.app.entity.Product;
import swd392.app.enums.NoteItemStatus;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface NoteItemRepository extends JpaRepository<NoteItem, String> {

    @Query("SELECT COALESCE(SUM(n.quantity), 0) FROM NoteItem n WHERE n.product.productCode = :productCode AND " +
            "n.exchangeNote.transactionType = 'IMPORT' AND n.exchangeNote.destinationWarehouse.warehouseCode = :warehouseCode " +
            "AND n.status = 'COMPLETED'")
    Integer getTotalImportByProductCodeAndWarehouse(@Param("productCode") String productCode,
                                                    @Param("warehouseCode") String warehouseCode);

    @Query("SELECT COALESCE(SUM(n.quantity), 0) FROM NoteItem n WHERE n.product.productCode = :productCode AND " +
            "n.exchangeNote.transactionType = 'EXPORT' AND n.exchangeNote.sourceWarehouse.warehouseCode = :warehouseCode " +
            "AND n.status = 'COMPLETED'")
    Integer getTotalExportByProductCodeAndWarehouse(@Param("productCode") String productCode,
                                                    @Param("warehouseCode") String warehouseCode);

    @Query("SELECT COALESCE(SUM(n.quantity), 0) FROM NoteItem n WHERE n.product.productCode = :productCode " +
            "AND n.exchangeNote.transactionType = 'IMPORT' " +
            "AND n.exchangeNote.destinationWarehouse.warehouseCode = :warehouseCode " +
            "AND n.status = 'COMPLETED' " +
            "AND n.exchangeNote.date > :lastStockCheckDate")
    Integer getTotalImportAfterLastCheck(@Param("productCode") String productCode,
                                         @Param("warehouseCode") String warehouseCode,
                                         @Param("lastStockCheckDate") LocalDateTime lastStockCheckDate);

    @Query("SELECT COALESCE(SUM(n.quantity), 0) FROM NoteItem n WHERE n.product.productCode = :productCode " +
            "AND n.exchangeNote.transactionType = 'EXPORT' " +
            "AND n.exchangeNote.sourceWarehouse.warehouseCode = :warehouseCode " +
            "AND n.status = 'COMPLETED' " +
            "AND n.exchangeNote.date > :lastStockCheckDate")
    Integer getTotalExportAfterLastCheck(@Param("productCode") String productCode,
                                         @Param("warehouseCode") String warehouseCode,
                                         @Param("lastStockCheckDate") LocalDateTime lastStockCheckDate);

    List<NoteItem> findByExchangeNote_ExchangeNoteId(String exchangeNoteId);

    Optional<NoteItem> findByExchangeNoteAndProduct(ExchangeNote exchangeNote , Product product);

    List<NoteItem> findByExchangeNoteAndStatus(ExchangeNote exchangeNote, NoteItemStatus status);

    // (Tuỳ chọn) Thêm nếu bạn muốn lọc theo trạng thái
    List<NoteItem> findByExchangeNote_ExchangeNoteIdAndStatus(String exchangeNoteId, NoteItemStatus status);

    @Query("SELECT COALESCE(SUM(CASE " +
            "WHEN n.exchangeNote.transactionType = 'IMPORT' THEN n.quantity " +
            "WHEN n.exchangeNote.transactionType = 'EXPORT' THEN -n.quantity " +
            "ELSE 0 END), 0) " +
            "FROM NoteItem n " +
            "WHERE n.product.productCode = :productCode " +
            "AND (n.exchangeNote.destinationWarehouse.warehouseCode = :warehouseCode " +
            "OR n.exchangeNote.sourceWarehouse.warehouseCode = :warehouseCode) " +
            "AND n.exchangeNote.status = 'finished'")
    Integer getTotalStockByProductAndWarehouse(@Param("productCode") String productCode,
                                               @Param("warehouseCode") String warehouseCode);

}

