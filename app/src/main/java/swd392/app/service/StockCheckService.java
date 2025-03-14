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

    // Map để lưu trữ StockCheckProduct tạm thời
    Map<String, List<StockCheckProduct>> temporaryStockCheckProducts = new HashMap<>();

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

        // Lưu phiếu kiểm kho nhưng CHƯA lưu chi tiết sản phẩm
        StockCheckNote savedStockCheckNote = stockCheckNoteRepository.save(stockCheckNote);

        // Tạo danh sách sản phẩm kiểm kho nhưng CHỈ LƯU VÀO MAP TẠM THỜI
        List<StockCheckProduct> stockCheckProducts = request.getStockCheckProducts().stream()
                .map(productRequest -> {
                    Product product = productRepository.findByProductCode(productRequest.getProductCode())
                            .orElseThrow(() -> new AppException(ErrorCode.PRODUCT_NOT_FOUND));

                    Integer totalImport = noteItemRepository.getTotalImportByProductCodeAndWarehouse(
                            product.getProductCode(), request.getWarehouseCode());
                    Integer totalExport = noteItemRepository.getTotalExportByProductCodeAndWarehouse(
                            product.getProductCode(), request.getWarehouseCode());

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

        // QUAN TRỌNG: Chỉ lưu vào bộ nhớ tạm thời, không lưu vào database
        // Lưu StockCheckProducts vào map tạm thời thay vì lưu cả StockCheckNote
        temporaryStockCheckProducts.put(savedStockCheckNote.getStockCheckNoteId(), stockCheckProducts);

        // Gán danh sách stockCheckProducts cho response nhưng không lưu vào database
        savedStockCheckNote.setStockCheckProducts(stockCheckProducts);

        log.info("Phiếu kiểm kho tạm thời được tạo: {}", savedStockCheckNote.getStockCheckNoteId());
        return stockCheckMapper.toStockCheckNoteResponse(savedStockCheckNote);
    }

    @PreAuthorize("hasRole('MANAGER')")
    @Transactional
    public StockCheckNoteResponse approveStockCheck(String stockCheckNoteId) {
        log.info("Duyệt phiếu kiểm kho: {}", stockCheckNoteId);

        // Lấy phiếu kiểm kho từ database
        StockCheckNote stockCheckNote = stockCheckNoteRepository.findById(stockCheckNoteId)
                .orElseThrow(() -> new AppException(ErrorCode.STOCK_CHECK_NOTE_NOT_FOUND));

        // Kiểm tra trạng thái hiện tại
        if (stockCheckNote.getStockCheckStatus() != StockCheckStatus.pending) {
            throw new AppException(ErrorCode.STOCK_CHECK_CANNOT_BE_MODIFIED);
        }

        // Cập nhật trạng thái thành accepted nhưng không lưu StockCheckProducts
        stockCheckNote.setStockCheckStatus(StockCheckStatus.accepted);
        stockCheckNoteRepository.save(stockCheckNote);

        // Tạo đối tượng response với các sản phẩm tạm thời nếu cần
        StockCheckNoteResponse response = stockCheckMapper.toStockCheckNoteResponse(stockCheckNote);

        log.info("Phiếu kiểm kho đã được duyệt: {}", stockCheckNoteId);
        return stockCheckMapper.toStockCheckNoteResponse(stockCheckNote);
    }

    @PreAuthorize("hasRole('MANAGER')")
    @Transactional
    public StockCheckNoteResponse finalizeStockCheck(String stockCheckNoteId, boolean isFinished) {
        log.info("Hoàn tất phiếu kiểm kho: {} - Trạng thái: {}", stockCheckNoteId, isFinished);

        // Lấy phiếu kiểm kho từ database
        StockCheckNote stockCheckNote = stockCheckNoteRepository.findById(stockCheckNoteId)
                .orElseThrow(() -> new AppException(ErrorCode.STOCK_CHECK_NOTE_NOT_FOUND));

        // Kiểm tra trạng thái hiện tại
        if (stockCheckNote.getStockCheckStatus() != StockCheckStatus.accepted) {
            throw new AppException(ErrorCode.STOCK_CHECK_CANNOT_BE_FINALIZED);
        }

        // Lấy danh sách các StockCheckProduct từ bản đồ tạm thời
        List<StockCheckProduct> stockCheckProducts = temporaryStockCheckProducts.get(stockCheckNoteId);

        if (stockCheckProducts == null || stockCheckProducts.isEmpty()) {
            throw new AppException(ErrorCode.STOCK_CHECK_PRODUCTS_NOT_FOUND);
        }

        if (isFinished) {
            // Xử lý và lưu các StockCheckProduct vào database
            for (StockCheckProduct product : stockCheckProducts) {
                if (product.getExpectedQuantity() == null) {
                    product.setExpectedQuantity(0);
                }
                if (product.getActualQuantity() == null) {
                    product.setActualQuantity(0);
                }
                product.setStockCheckNote(stockCheckNote);
            }

            // QUAN TRỌNG: Lưu tất cả các StockCheckProduct vào database trước
            stockCheckProducts = stockCheckProductRepository.saveAll(stockCheckProducts);

            // Sau đó mới cập nhật stockCheckNote
            stockCheckNote.setStockCheckProducts(stockCheckProducts);
            stockCheckNote.setStockCheckStatus(StockCheckStatus.finished);
        } else {
            // Từ chối phiếu kiểm kho, không lưu StockCheckProducts
            stockCheckNote.setStockCheckStatus(StockCheckStatus.rejected);
        }

        // Lưu phiếu kiểm kho và xóa dữ liệu tạm thời
        stockCheckNoteRepository.save(stockCheckNote);
        temporaryStockCheckProducts.remove(stockCheckNoteId);

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