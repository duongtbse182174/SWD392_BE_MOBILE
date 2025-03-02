package swd392.app.service;

import jakarta.transaction.Transactional;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import swd392.app.dto.request.StockCheckNoteRequest;
import swd392.app.dto.request.StockCheckProductRequest;
import swd392.app.dto.response.StockCheckNoteResponse;
import swd392.app.entity.*;
import swd392.app.exception.AppException;
import swd392.app.exception.ErrorCode;
import swd392.app.mapper.StockCheckMapper;
import swd392.app.repository.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class StockCheckService {
    StockCheckNoteRepository stockCheckNoteRepository;
    StockCheckProductRepository stockCheckProductRepository;
    UserRepository userRepository;
    StockRepository stockRepository;
    ProductRepository productRepository;
    WarehouseRepository warehouseRepository;
    StockCheckMapper stockCheckMapper;

    @Transactional
    public StockCheckNoteResponse createStockCheckNote(StockCheckNoteRequest request) {
        log.info("Creating stock check note for warehouse: {}", request.getWarehouseCode());

        // Lấy thông tin người dùng hiện tại
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null) {
            throw new AppException(ErrorCode.UNAUTHENTICATED);
        }

        String userName = authentication.getName();
        User checker = userRepository.findByEmail(userName)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXIST));

        // Lấy thông tin kho
        Warehouse warehouse = warehouseRepository.findByWarehouseCode(request.getWarehouseCode())
                .orElseThrow(() -> new AppException(ErrorCode.WAREHOUSE_NOT_FOUND));

        // Tạo phiếu kiểm kho mới
        StockCheckNote stockCheckNote = new StockCheckNote();
        stockCheckNote.setStockCheckNoteId(UUID.randomUUID().toString());
        stockCheckNote.setDate(LocalDate.now());
        stockCheckNote.setWarehouse(warehouse);
        stockCheckNote.setChecker(checker);

        // Lưu phiếu kiểm kho
        StockCheckNote savedNote = stockCheckNoteRepository.save(stockCheckNote);

        // Xử lý các sản phẩm kiểm kho
        List<StockCheckProduct> stockCheckProducts = new ArrayList<>();

        for (StockCheckProductRequest productRequest : request.getStockCheckProducts()) {
            // Tìm sản phẩm
            Product product = productRepository.findByProductCode(productRequest.getProductCode())
                    .orElseThrow(() -> new AppException(ErrorCode.PRODUCT_NOT_FOUND));

            // Tìm thông tin tồn kho hiện tại
            Stock stock = stockRepository.findByProduct_ProductCode(productRequest.getProductCode());
            if (stock == null) {
                throw new AppException(ErrorCode.STOCK_NOT_FOUND);
            }

            Integer expectedQuantity = stock.getQuantity();
            Integer actualQuantity = productRequest.getActualQuantity();

            // Tạo chi tiết kiểm kho
            StockCheckProduct stockCheckProduct = new StockCheckProduct();
            stockCheckProduct.setStockCheckProductId(UUID.randomUUID().toString());
            stockCheckProduct.setStockCheckNote(savedNote);
            stockCheckProduct.setProduct(product);
            stockCheckProduct.setExpectedQuantity(expectedQuantity);
            stockCheckProduct.setActualQuantity(actualQuantity);

            stockCheckProducts.add(stockCheckProduct);

            // Cập nhật số lượng tồn kho
            stock.setQuantity(actualQuantity);
            stockRepository.save(stock);

            // Cập nhật số lượng trong bảng Product
            product.setQuantity(actualQuantity);
            productRepository.save(product);
        }

        // Lưu chi tiết kiểm kho
        stockCheckProductRepository.saveAll(stockCheckProducts);
        savedNote.setStockCheckProducts(stockCheckProducts);

        // Chuyển đổi sang response sử dụng mapper
        return stockCheckMapper.toStockCheckNoteResponse(savedNote);
    }

    public List<StockCheckNoteResponse> getAllStockCheckNotes() {
        List<StockCheckNote> notes = stockCheckNoteRepository.findAll();
        return notes.stream()
                .map(stockCheckMapper::toStockCheckNoteResponse) // Sử dụng mapper
                .collect(Collectors.toList());
    }

    public List<StockCheckNoteResponse> getStockCheckNotesByWarehouse(String warehouseCode) {
        List<StockCheckNote> notes = stockCheckNoteRepository.findByWarehouse_WarehouseCode(warehouseCode);
        return notes.stream()
                .map(stockCheckMapper::toStockCheckNoteResponse) // Sử dụng mapper
                .collect(Collectors.toList());
    }
}
