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
import swd392.app.dto.request.StockCheckProductRequest;
import swd392.app.dto.response.StockCheckNoteResponse;
import swd392.app.entity.*;
import swd392.app.enums.StockCheckProductStatus;
import swd392.app.enums.StockCheckStatus;
import swd392.app.exception.AppException;
import swd392.app.exception.ErrorCode;
import swd392.app.mapper.StockCheckMapper;
import swd392.app.repository.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
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
    ProductRepository productRepository;
    WarehouseRepository warehouseRepository;
    StockCheckMapper stockCheckMapper;
    NoteItemRepository noteItemRepository;

    /**
     * Tạo phiếu kiểm kho tạm thời.
     */
    @PreAuthorize("hasRole('STAFF')")
    public StockCheckNoteResponse createStockCheckNote(StockCheckNoteRequest request) {
        log.info("Tạo phiếu kiểm kho tạm thời cho kho: {}", request.getWarehouseCode());

        User checker = getAuthenticatedUserAndValidateWarehouse(request.getWarehouseCode());
        Warehouse warehouse = getWarehouseByCode(request.getWarehouseCode());

        // Tạo phiếu kiểm kho
        StockCheckNote stockCheckNote = createNewStockCheckNote(checker, warehouse, request.getDescription());
        StockCheckNote savedStockCheckNote = stockCheckNoteRepository.save(stockCheckNote);

        // Tạo và lưu các sản phẩm kiểm kho
        List<StockCheckProduct> stockCheckProducts = createStockCheckProducts(request.getStockCheckProducts(), savedStockCheckNote, warehouse);
        stockCheckProductRepository.saveAll(stockCheckProducts);
        savedStockCheckNote.setStockCheckProducts(stockCheckProducts);

        log.info("Phiếu kiểm kho tạm thời được tạo: {}", savedStockCheckNote.getStockCheckNoteId());
        return stockCheckMapper.toStockCheckNoteResponse(savedStockCheckNote);
    }

    /**
     * Tạo danh sách sản phẩm kiểm kho.
     */
    private List<StockCheckProduct> createStockCheckProducts(List<StockCheckProductRequest> productRequests,
                                                             StockCheckNote stockCheckNote,
                                                             Warehouse warehouse) {
        return productRequests.stream()
                .map(request -> createStockCheckProduct(request, stockCheckNote, warehouse))
                .collect(Collectors.toList());
    }

    /**
     * Lấy thông tin người dùng hiện tại.
     */
    private User getAuthenticatedUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null) {
            throw new AppException(ErrorCode.UNAUTHENTICATED);
        }

        String userName = authentication.getName();
        return userRepository.findByEmail(userName)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXIST));
    }

    /**
     * Lấy thông tin người dùng hiện tại và kiểm tra quyền truy cập kho.
     */
    private User getAuthenticatedUserAndValidateWarehouse(String warehouseCode) {
        User user = getAuthenticatedUser();

        if (!user.getWarehouse().getWarehouseCode().equals(warehouseCode)) {
            throw new AppException(ErrorCode.UNAUTHORIZED_ACTION);
        }
        return user;
    }

    /**
     * Lấy thông tin kho theo mã kho.
     */
    private Warehouse getWarehouseByCode(String warehouseCode) {
        return warehouseRepository.findByWarehouseCode(warehouseCode)
                .orElseThrow(() -> new AppException(ErrorCode.WAREHOUSE_NOT_FOUND));
    }

    /**
     * Tạo một phiếu kiểm kho mới với trạng thái pending.
     */
    private StockCheckNote createNewStockCheckNote(User checker, Warehouse warehouse, String description) {
        StockCheckNote stockCheckNote = new StockCheckNote();
        stockCheckNote.setStockCheckNoteId(UUID.randomUUID().toString());
        stockCheckNote.setDate(LocalDate.now());
        stockCheckNote.setWarehouse(warehouse);
        stockCheckNote.setChecker(checker);
        stockCheckNote.setDescription(description);
        stockCheckNote.setStockCheckStatus(StockCheckStatus.pending);
        return stockCheckNote;
    }

    /**
     * Tạo một sản phẩm kiểm kho từ request.
     */
    private StockCheckProduct createStockCheckProduct(StockCheckProductRequest productRequest,
                                                      StockCheckNote stockCheckNote, Warehouse warehouse) {
        Product product = productRepository.findByProductCode(productRequest.getProductCode())
                .orElseThrow(() -> new AppException(ErrorCode.PRODUCT_NOT_FOUND));

        String warehouseCode = warehouse.getWarehouseCode();
        String productCode = product.getProductCode();

        // Lấy thông tin số lượng nhập, xuất
        Integer totalImport = Optional.ofNullable(noteItemRepository.getTotalImportByProductCodeAndWarehouse(
                productCode, warehouseCode)).orElse(0);
        Integer totalExport = Optional.ofNullable(noteItemRepository.getTotalExportByProductCodeAndWarehouse(
                productCode, warehouseCode)).orElse(0);

        // Lấy thông tin kiểm kho gần nhất
        Optional<StockCheckProduct> lastStockCheck = stockCheckProductRepository
                .findLatestStockCheck(productCode, warehouseCode);

        // Tạo đối tượng sản phẩm kiểm kho
        StockCheckProduct stockCheckProduct = new StockCheckProduct();
        stockCheckProduct.setStockCheckProductId(UUID.randomUUID().toString());
        stockCheckProduct.setStockCheckNote(stockCheckNote);
        stockCheckProduct.setProduct(product);
        stockCheckProduct.setLastQuantity(lastStockCheck.map(StockCheckProduct::getActualQuantity).orElse(0));
        stockCheckProduct.setActualQuantity(productRequest.getActualQuantity());
        stockCheckProduct.setTotalImportQuantity(totalImport);
        stockCheckProduct.setTotalExportQuantity(totalExport);
        stockCheckProduct.calculateTheoreticalQuantity();
        stockCheckProduct.setStockCheckProductStatus(StockCheckProductStatus.temporary);

        return stockCheckProduct;
    }

    /**
     * Duyệt phiếu kiểm kho.
     */
    @PreAuthorize("hasRole('MANAGER')")
    @Transactional
    public StockCheckNoteResponse approveStockCheck(String stockCheckNoteId) {
        log.info("Duyệt phiếu kiểm kho: {}", stockCheckNoteId);

        // Lấy thông tin phiếu kiểm kho và kiểm tra quyền truy cập
        StockCheckNote stockCheckNote = getAndValidateStockCheckNote(stockCheckNoteId);

        // Kiểm tra trạng thái phiếu
        if (stockCheckNote.getStockCheckStatus() != StockCheckStatus.pending) {
            throw new AppException(ErrorCode.STOCK_CHECK_CANNOT_BE_MODIFIED);
        }

        // Cập nhật trạng thái và lưu
        stockCheckNote.setStockCheckStatus(StockCheckStatus.accepted);
        stockCheckNoteRepository.save(stockCheckNote);

        log.info("Phiếu kiểm kho đã được duyệt: {}", stockCheckNoteId);
        return stockCheckMapper.toStockCheckNoteResponse(stockCheckNote);
    }

    /**
     * Lấy phiếu kiểm kho theo ID và xác thực quyền truy cập.
     */
    private StockCheckNote getAndValidateStockCheckNote(String stockCheckNoteId) {
        StockCheckNote stockCheckNote = getStockCheckNoteById(stockCheckNoteId);
        User manager = getAuthenticatedUser();
        validateManagerWarehouse(manager, stockCheckNote.getWarehouse().getWarehouseCode());
        return stockCheckNote;
    }

    /**
     * Lấy phiếu kiểm kho theo ID.
     */
    private StockCheckNote getStockCheckNoteById(String stockCheckNoteId) {
        return stockCheckNoteRepository.findById(stockCheckNoteId)
                .orElseThrow(() -> new AppException(ErrorCode.STOCK_CHECK_NOTE_NOT_FOUND));
    }

    /**
     * Kiểm tra quyền truy cập kho của manager.
     */
    private void validateManagerWarehouse(User manager, String warehouseCode) {
        if (!manager.getWarehouse().getWarehouseCode().equals(warehouseCode)) {
            throw new AppException(ErrorCode.UNAUTHORIZED_ACTION);
        }
    }

    /**
     * Hoàn tất hoặc từ chối phiếu kiểm kho.
     */
    @PreAuthorize("hasRole('MANAGER')")
    @Transactional
    public StockCheckNoteResponse finalizeStockCheck(String stockCheckNoteId, boolean isFinished) {
        log.info("Hoàn tất phiếu kiểm kho: {} - Trạng thái: {}", stockCheckNoteId, isFinished);

        // Lấy thông tin phiếu kiểm kho và kiểm tra quyền truy cập
        StockCheckNote stockCheckNote = getAndValidateStockCheckNote(stockCheckNoteId);

        // Kiểm tra trạng thái phiếu
        if (stockCheckNote.getStockCheckStatus() != StockCheckStatus.accepted) {
            throw new AppException(ErrorCode.STOCK_CHECK_CANNOT_BE_FINALIZED);
        }

        // Lấy danh sách sản phẩm kiểm kho tạm thời
        List<StockCheckProduct> stockCheckProducts = stockCheckProductRepository
                .findByStockCheckNoteAndStockCheckProductStatus(stockCheckNote, StockCheckProductStatus.temporary);

        if (stockCheckProducts.isEmpty()) {
            throw new AppException(ErrorCode.STOCK_CHECK_PRODUCTS_NOT_FOUND);
        }

        // Cập nhật trạng thái phiếu và sản phẩm
        updateStockCheckStatus(stockCheckNote, stockCheckProducts, isFinished);

        // Lưu thông tin phiếu kiểm kho
        StockCheckNote updatedNote = stockCheckNoteRepository.save(stockCheckNote);

        log.info("Phiếu kiểm kho đã hoàn tất: {}", stockCheckNote.getStockCheckNoteId());
        return stockCheckMapper.toStockCheckNoteResponse(stockCheckNote);
    }

    /**
     * Cập nhật trạng thái phiếu kiểm kho và sản phẩm.
     */
    private void updateStockCheckStatus(StockCheckNote stockCheckNote, List<StockCheckProduct> products, boolean isFinished) {
        if (isFinished) {
            // Cập nhật trạng thái sản phẩm thành hoàn tất
            products.forEach(product -> {
                product.setExpectedQuantity(Optional.ofNullable(product.getExpectedQuantity()).orElse(0));
                product.setActualQuantity(Optional.ofNullable(product.getActualQuantity()).orElse(0));
                product.setStockCheckProductStatus(StockCheckProductStatus.finished);
            });
            stockCheckProductRepository.saveAll(products);
            stockCheckNote.setStockCheckStatus(StockCheckStatus.finished);
        } else {
            // Từ chối phiếu kiểm kho và xóa sản phẩm
            stockCheckNote.setStockCheckStatus(StockCheckStatus.rejected);
            stockCheckProductRepository.deleteAll(products);
        }
    }

    /**
     * Lấy tất cả phiếu kiểm kho.
     */
    public List<StockCheckNoteResponse> getAllStockCheckNotes() {
        List<StockCheckNote> stockCheckNotes = stockCheckNoteRepository.findAll();
        return stockCheckNotes.stream()
                .map(stockCheckMapper::toStockCheckNoteResponse)
                .collect(Collectors.toList());
    }

    /**
     * Lấy phiếu kiểm kho theo mã kho.
     */
    public List<StockCheckNoteResponse> getStockCheckNotesByWarehouse(String warehouseCode) {
        List<StockCheckNote> stockCheckNotes = stockCheckNoteRepository.findByWarehouse_WarehouseCode(warehouseCode);
        return stockCheckNotes.stream()
                .map(stockCheckMapper::toStockCheckNoteResponse)
                .collect(Collectors.toList());
    }
}