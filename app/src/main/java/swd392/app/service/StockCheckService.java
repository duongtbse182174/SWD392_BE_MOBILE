package swd392.app.service;

import jakarta.transaction.Transactional;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import swd392.app.dto.request.StockCheckNoteRequest;
import swd392.app.dto.response.StockCheckNoteResponse;
import swd392.app.entity.*;
import swd392.app.enums.StockCheckStatus;
import swd392.app.exception.AppException;
import swd392.app.exception.ErrorCode;
import swd392.app.mapper.StockCheckMapper;
import swd392.app.repository.*;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class StockCheckService {

    StockCheckNoteRepository stockCheckNoteRepository;
    StockCheckProductRepository stockCheckProductRepository;
    UserRepository userRepository;
    ProductRepository productRepository;
    WarehouseRepository warehouseRepository;
    StockCheckMapper stockCheckMapper;
    NoteItemRepository noteItemRepository;

    Map<String, StockCheckNote> temporaryStockCheckNotes = new HashMap<>();

    @PreAuthorize("hasRole('STAFF')")
    public StockCheckNoteResponse createStockCheckNote(StockCheckNoteRequest request) {
        log.info("Tạo phiếu kiểm kho tạm thời cho kho: {}", request.getWarehouseCode());

        // Xác định người thực hiện kiểm kho
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

        StockCheckNote savedStockCheckNote = stockCheckNoteRepository.save(stockCheckNote);

        // Tạo danh sách sản phẩm kiểm kho
        List<StockCheckProduct> stockCheckProducts = request.getStockCheckProducts().stream()
                .map(productRequest -> {
                    Product product = productRepository.findByProductCode(productRequest.getProductCode())
                            .orElseThrow(() -> new AppException(ErrorCode.PRODUCT_NOT_FOUND));

                    Integer totalImport = noteItemRepository.getTotalImportByProductCode(product.getProductCode());
                    Integer totalExport = noteItemRepository.getTotalExportByProductCode(product.getProductCode());

                    StockCheckProduct stockCheckProduct = new StockCheckProduct();
                    stockCheckProduct.setStockCheckProductId(UUID.randomUUID().toString());
                    stockCheckProduct.setStockCheckNote(savedStockCheckNote);
                    stockCheckProduct.setProduct(product);

                    Optional<StockCheckProduct> lastStockCheckProduct =
                            stockCheckProductRepository.findTopByProductAndStockCheckNoteWarehouseOrderByStockCheckNoteDateDesc(product, warehouse);

                    stockCheckProduct.setLastQuantity(lastStockCheckProduct.map(StockCheckProduct::getActualQuantity).orElse(0));

                    stockCheckProduct.setActualQuantity(productRequest.getActualQuantity());
                    stockCheckProduct.setTotalImportQuantity(totalImport != null ? totalImport : 0);
                    stockCheckProduct.setTotalExportQuantity(totalExport != null ? totalExport : 0);
                    stockCheckProduct.calculateTheoreticalQuantity();

                    return stockCheckProduct;
                }).collect(Collectors.toList());

        savedStockCheckNote.setStockCheckProducts(stockCheckProducts);
        temporaryStockCheckNotes.put(savedStockCheckNote.getStockCheckNoteId(), savedStockCheckNote);

        log.info("Phiếu kiểm kho tạm thời được tạo: {}", savedStockCheckNote.getStockCheckNoteId());
        return stockCheckMapper.toStockCheckNoteResponse(savedStockCheckNote);
    }

    @PreAuthorize("hasRole('MANAGER')")
    @Transactional
    public StockCheckNoteResponse approveStockCheck(String stockCheckNoteId) {
        log.info("Duyệt phiếu kiểm kho: {}", stockCheckNoteId);

        StockCheckNote stockCheckNote = temporaryStockCheckNotes.get(stockCheckNoteId);
        if (stockCheckNote == null) {
            stockCheckNote = stockCheckNoteRepository.findById(stockCheckNoteId)
                    .orElseThrow(() -> new AppException(ErrorCode.STOCK_CHECK_NOTE_NOT_FOUND));
        }

        stockCheckNote.setStockCheckStatus(StockCheckStatus.accepted);
        stockCheckNoteRepository.save(stockCheckNote);

        log.info("Phiếu kiểm kho đã được duyệt: {}", stockCheckNoteId);
        return stockCheckMapper.toStockCheckNoteResponse(stockCheckNote);
    }

    @PreAuthorize("hasRole('MANAGER')")
    @Transactional
    public StockCheckNoteResponse finalizeStockCheck(String stockCheckNoteId, boolean isFinished) {
        log.info("Hoàn tất phiếu kiểm kho: {} - Trạng thái: {}", stockCheckNoteId, isFinished);

        StockCheckNote stockCheckNote = temporaryStockCheckNotes.get(stockCheckNoteId);
        if (stockCheckNote == null) {
            stockCheckNote = stockCheckNoteRepository.findById(stockCheckNoteId)
                    .orElseThrow(() -> new AppException(ErrorCode.STOCK_CHECK_NOTE_NOT_FOUND));
        }

        if (!isFinished) {
            log.info("Phiếu kiểm kho bị từ chối: {}", stockCheckNoteId);
            stockCheckNote.setStockCheckStatus(StockCheckStatus.rejected);
        } else {
            for (StockCheckProduct product : stockCheckNote.getStockCheckProducts()) {
                if (product.getExpectedQuantity() == null) {
                    product.setExpectedQuantity(0);
                }
                if (product.getActualQuantity() == null) {
                    product.setActualQuantity(0);
                }

                Product existingProduct = productRepository.findByProductCode(product.getProduct().getProductCode())
                        .orElseThrow(() -> new AppException(ErrorCode.PRODUCT_NOT_FOUND));

                product.setStockCheckNote(stockCheckNote);
            }

            stockCheckNote.setStockCheckStatus(StockCheckStatus.finished);
            stockCheckProductRepository.saveAll(stockCheckNote.getStockCheckProducts());
        }

        stockCheckNoteRepository.save(stockCheckNote);
        temporaryStockCheckNotes.remove(stockCheckNoteId);

        log.info("Phiếu kiểm kho đã hoàn tất: {}", stockCheckNote.getStockCheckNoteId());
        return stockCheckMapper.toStockCheckNoteResponse(stockCheckNote);
    }

    public List<StockCheckNoteResponse> getAllStockCheckNotes() {
        return stockCheckNoteRepository.findAll().stream()
                .map(stockCheckMapper::toStockCheckNoteResponse)
                .collect(Collectors.toList());
    }

    public List<StockCheckNoteResponse> getStockCheckNotesByWarehouse(String warehouseCode) {
        return stockCheckNoteRepository.findByWarehouse_WarehouseCode(warehouseCode).stream()
                .map(stockCheckMapper::toStockCheckNoteResponse)
                .collect(Collectors.toList());
    }
}