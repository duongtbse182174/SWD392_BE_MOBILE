package swd392.app.repository;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import swd392.app.entity.ExchangeNote;

import java.util.List;
import java.util.Optional;

@Repository
public interface StockTransactionRepository extends JpaRepository<ExchangeNote, String> {
    List<ExchangeNote> findByDestinationWarehouse_WarehouseCode(String warehouseCode);

    @EntityGraph(attributePaths = "noteItems") // Load luôn các NoteItem khi lấy ExchangeNote
    Optional<ExchangeNote> findById(String exchangeNoteId);

}
