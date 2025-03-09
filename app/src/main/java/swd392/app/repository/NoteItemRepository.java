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
    Optional<NoteItem> findByProduct_ProductCodeAndExchangeNote_DestinationWarehouse_WarehouseId(
            String productCode, String warehouseId);

    @Query("SELECT ni FROM NoteItem ni WHERE ni.exchangeNote.exchangeNoteId = :exchangeNoteId")
    List<NoteItem> findByExchangeNote_ExchangeNoteId(@Param("exchangeNoteId") String exchangeNoteId);

}
