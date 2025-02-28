package swd392.app.service;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import swd392.app.dto.response.StockResponse;
import swd392.app.entity.Stock;
import swd392.app.mapper.StockMapper;
import swd392.app.repository.StockRepository;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class InventoryService {
    StockRepository stockRepository;
    StockMapper stockMapper;

    public List<StockResponse> getAllStocks() {
        List<Stock> stocks = stockRepository.findAll();
        return stocks.stream()
                .map(stockMapper::toStockResponse)
                .collect(Collectors.toList());
    }

    public StockResponse getStockByProductCode(String productCode) {
        Stock stock = stockRepository.findByProduct_ProductCode(productCode);
        return stockMapper.toStockResponse(stock);
    }

    // Thêm các phương thức khác nếu cần
}