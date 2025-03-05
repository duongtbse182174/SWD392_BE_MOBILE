package swd392.app.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import swd392.app.entity.StockCheckProduct;

import java.util.List;

@Repository
public interface StockCheckProductRepository extends JpaRepository<StockCheckProduct, String> {
//    List<StockCheckProduct> findByStockCheckNoteId(String stockCheckNote);
}
