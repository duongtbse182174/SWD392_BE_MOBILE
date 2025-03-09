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
import swd392.app.dto.request.StockCheckApprovalRequest;
import swd392.app.dto.request.StockCheckNoteRequest;
import swd392.app.dto.request.StockCheckProductRequest;
import swd392.app.dto.response.StockCheckNoteResponse;
import swd392.app.entity.*;
import swd392.app.enums.StockCheckStatus;
import swd392.app.exception.AppException;
import swd392.app.exception.ErrorCode;
import swd392.app.mapper.StockCheckMapper;
import swd392.app.mapper.UserMapper;
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

    public StockCheckNoteResponse createStockCheckNote(StockCheckNoteRequest request) {
        log.info("Creating stock check note for warehouse: {}", request.getWarehouseCode());

        try {
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
            stockCheckNote.setDescription(request.getDescription());
            stockCheckNote.setStockCheckStatus(StockCheckStatus.pending);

            // Lưu phiếu kiểm kho
            StockCheckNote savedNote = stockCheckNoteRepository.save(stockCheckNote);

            // Xử lý các sản phẩm kiểm kho
            List<StockCheckProduct> stockCheckProducts = new ArrayList<>();

            for (StockCheckProductRequest productRequest : request.getStockCheckProducts()) {
                // Tìm sản phẩm
                Product product = productRepository.findByProductCode(productRequest.getProductCode())
                        .orElseThrow(() -> new AppException(ErrorCode.PRODUCT_NOT_FOUND));

                log.info("Processing product: {}", productRequest.getProductCode());

                // Lấy số lượng hiện tại trực tiếp từ Product
                int lastQuantity = product.getQuantity();
                int actualQuantity = productRequest.getActualQuantity();


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

            log.info("StockCheckNote saved: {}", savedNote);
            log.info("StockCheckProducts size: {}", savedNote.getStockCheckProducts().size());

            // Chuyển đổi sang response sử dụng mapper
            return stockCheckMapper.toStockCheckNoteResponse(savedNote);

        } catch (AppException e) {
            log.error("AppException occurred: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("Unexpected error: ", e);
            throw new AppException(ErrorCode.UNKNOWN_ERROR);
        }
    }

    public StockCheckNoteResponse approveStockCheck(StockCheckApprovalRequest request) {
        log.info("Approving stock check note: {}", request.getStockCheckNoteId());

        // Tìm phiếu kiểm kho
        StockCheckNote stockCheckNote = stockCheckNoteRepository.findById(request.getStockCheckNoteId())
                .orElseThrow(() -> new AppException(ErrorCode.STOCK_CHECK_NOTE_NOT_FOUND));

        // Chỉ cho phép cập nhật nếu trạng thái hiện tại là PENDING
        if (stockCheckNote.getStockCheckStatus() != StockCheckStatus.pending) {
            throw new AppException(ErrorCode.STOCK_CHECK_NOTE_CANNOT_BE_MODIFIED);
        }

        // Chuyển sang trạng thái ACCEPTED
        stockCheckNote.setStockCheckStatus(StockCheckStatus.accepted);
        stockCheckNoteRepository.save(stockCheckNote);
        log.info("Stock check note status updated to ACCEPTED");

        return stockCheckMapper.toStockCheckNoteResponse(stockCheckNote);
    }

    public StockCheckNoteResponse finalizeStockCheck(String stockCheckNoteId, boolean isFinished) {
        log.info("Finalizing stock check note: {}", stockCheckNoteId);

        StockCheckNote stockCheckNote = stockCheckNoteRepository.findById(stockCheckNoteId)
                .orElseThrow(() -> new AppException(ErrorCode.STOCK_CHECK_NOTE_NOT_FOUND));

        // Chỉ có thể chuyển từ ACCEPTED → FINISHED hoặc REJECTED
        if (stockCheckNote.getStockCheckStatus() != StockCheckStatus.accepted) {
            throw new AppException(ErrorCode.STOCK_CHECK_NOTE_CANNOT_BE_FINALIZED);
        }

        stockCheckNote.setStockCheckStatus(isFinished ? StockCheckStatus.finished : StockCheckStatus.rejected);
        stockCheckNoteRepository.save(stockCheckNote);

        log.info("Stock check note finalized as: {}", stockCheckNote.getStockCheckStatus());
        return stockCheckMapper.toStockCheckNoteResponse(stockCheckNote);
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