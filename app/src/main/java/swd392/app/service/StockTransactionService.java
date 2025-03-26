package swd392.app.service;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;
import swd392.app.dto.request.StockExchangeRequest;
import swd392.app.dto.request.TransactionItemRequest;
import swd392.app.dto.response.StockExchangeResponse;
import swd392.app.dto.response.NoteItemResponse;
import swd392.app.entity.*;
import swd392.app.enums.NoteItemStatus;
import swd392.app.enums.ProductStatus;
import swd392.app.enums.StockExchangeStatus;
import swd392.app.enums.StockTransactionType;
import swd392.app.exception.AppException;
import swd392.app.exception.ErrorCode;
import swd392.app.mapper.StockTransactionMapper;
import swd392.app.mapper.NoteItemMapper;
import swd392.app.repository.*;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class StockTransactionService {

    StockTransactionRepository stockTransactionRepository;
    NoteItemRepository noteItemRepository;
    ProductRepository productRepository;
    WarehouseRepository warehouseRepository;
    UserRepository userRepository;
    StockTransactionMapper stockTransactionMapper;
    NoteItemMapper noteItemMapper;

    //    @PreAuthorize("hasRole('STAFF')")
    public StockExchangeResponse createTransaction(StockExchangeRequest request) {
        log.info("Bắt đầu tạo giao dịch: {}", request);

        Warehouse sourceWarehouse = null;
        Warehouse destinationWarehouse = null;

        if (request.getTransactionType() != StockTransactionType.IMPORT) {
            sourceWarehouse = warehouseRepository.findByWarehouseCode(request.getSourceWarehouseCode())
                    .orElseThrow(() -> new AppException(ErrorCode.WAREHOUSE_NOT_FOUND));
        }

        if (request.getTransactionType() != StockTransactionType.EXPORT) {
            destinationWarehouse = warehouseRepository.findByWarehouseCode(request.getDestinationWarehouseCode())
                    .orElseThrow(() -> new AppException(ErrorCode.WAREHOUSE_NOT_FOUND));
        }

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null) throw new AppException(ErrorCode.UNAUTHENTICATED);

        User checker = userRepository.findByUserName(authentication.getName())
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXIST));

        // Kiểm tra các mục trước khi tạo phiếu (dựa trên stock check)
        validateItems(request.getItems(), sourceWarehouse, destinationWarehouse, request.getTransactionType());

        // Tạo ExchangeNote sau khi kiểm tra thành công
        ExchangeNote transaction = new ExchangeNote();
        transaction.setExchangeNoteId(UUID.randomUUID().toString());
        transaction.setDate(LocalDate.now());
        transaction.setTransactionType(request.getTransactionType());
        transaction.setSourceWarehouse(sourceWarehouse);
        transaction.setDestinationWarehouse(destinationWarehouse);
        transaction.setCreatedBy(checker);
        transaction.setStatus(StockExchangeStatus.pending);

        ExchangeNote savedTransaction = stockTransactionRepository.save(transaction);

        // Tạo hoặc cập nhật NoteItems
        List<NoteItem> noteItems = createOrUpdateNoteItems(
                request.getItems(), savedTransaction, sourceWarehouse, destinationWarehouse, request.getTransactionType()
        );

        noteItemRepository.saveAll(noteItems);

        transaction.setNoteItems(noteItems);

        List<NoteItemResponse> noteItemResponses = noteItems.stream()
                .map(noteItemMapper::toResponse)
                .collect(Collectors.toList());

        StockExchangeResponse response = stockTransactionMapper.toResponse(transaction);
        response.setItems(noteItemResponses);

        return response;
    }

    @PreAuthorize("hasRole('MANAGER')")
    public List<StockExchangeResponse> getAllTransactions() {
        log.info("Lấy tất cả giao dịch");
        return stockTransactionRepository.findAll().stream()
                .map(this::buildExchangeResponse)
                .collect(Collectors.toList());
    }

    @PreAuthorize("hasRole('MANAGER')")
    public List<StockExchangeResponse> getTransactionsByWarehouse(String warehouseCode) {
        log.info("Lấy giao dịch theo mã kho: {}", warehouseCode);
        return stockTransactionRepository.findByDestinationWarehouse_WarehouseCode(warehouseCode).stream()
                .map(this::buildExchangeResponse)
                .collect(Collectors.toList());
    }

    @PreAuthorize("hasRole('MANAGER')")
    public List<StockExchangeResponse> getPendingTransactions() {
        return stockTransactionRepository.findByStatus(StockExchangeStatus.pending).stream()
                .map(stockTransactionMapper::toResponse)
                .collect(Collectors.toList());
    }

    @PreAuthorize("hasRole('MANAGER') or hasRole('STAFF')")
    public StockExchangeResponse getTransactionById(String exchangeNoteId) {
        log.info("Lấy thông tin giao dịch với mã: {}", exchangeNoteId);

        ExchangeNote exchangeNote = stockTransactionRepository.findById(exchangeNoteId)
                .orElseThrow(() -> new AppException(ErrorCode.TRANSACTION_NOT_FOUND));

        return buildExchangeResponse(exchangeNote);
    }

    @PreAuthorize("hasRole('MANAGER')")
    public StockExchangeResponse approveTransaction(String exchangeNoteId) {
        log.info("Phê duyệt giao dịch với mã: {}", exchangeNoteId);

        ExchangeNote exchangeNote = stockTransactionRepository.findById(exchangeNoteId)
                .orElseThrow(() -> new AppException(ErrorCode.TRANSACTION_NOT_FOUND));

        if (exchangeNote.getStatus() != StockExchangeStatus.pending) {
            throw new AppException(ErrorCode.TRANSACTION_CANNOT_BE_MODIFIED);
        }

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null) throw new AppException(ErrorCode.UNAUTHENTICATED);

        User approveBy = userRepository.findByUserName(authentication.getName())
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXIST));

        exchangeNote.setStatus(StockExchangeStatus.accepted);
        exchangeNote.setApprovedBy(approveBy);
        stockTransactionRepository.save(exchangeNote);

        log.info("Giao dịch {} đã được phê duyệt thành công.", exchangeNoteId);
        return stockTransactionMapper.toResponse(exchangeNote);
    }

    @PreAuthorize("hasRole('MANAGER')")
    public StockExchangeResponse cancelTransaction(String exchangeNoteId) {
        log.info("Hủy giao dịch với mã: {}", exchangeNoteId);

        ExchangeNote exchangeNote = stockTransactionRepository.findById(exchangeNoteId)
                .orElseThrow(() -> new AppException(ErrorCode.TRANSACTION_NOT_FOUND));

        if (exchangeNote.getStatus() != StockExchangeStatus.accepted) {
            throw new AppException(ErrorCode.TRANSACTION_CANNOT_BE_FINALIZED);
        }

        List<NoteItem> noteItems = noteItemRepository.findByExchangeNote_ExchangeNoteId(exchangeNoteId);

        for (NoteItem noteItem : noteItems) {
            noteItem.setStatus(NoteItemStatus.CANCELED);
        }
        noteItemRepository.saveAll(noteItems);

        exchangeNote.setStatus(StockExchangeStatus.rejected);
        stockTransactionRepository.save(exchangeNote);

        log.info("Giao dịch đã bị từ chối thành công.");
        return stockTransactionMapper.toResponse(exchangeNote);
    }

    @PreAuthorize("hasRole('MANAGER')")
    public StockExchangeResponse finalizeTransaction(String exchangeNoteId) {
        log.info("Hoàn tất giao dịch với mã: {}", exchangeNoteId);

        ExchangeNote exchangeNote = stockTransactionRepository.findById(exchangeNoteId)
                .orElseThrow(() -> new AppException(ErrorCode.TRANSACTION_NOT_FOUND));

        if (exchangeNote.getStatus() != StockExchangeStatus.accepted) {
            throw new AppException(ErrorCode.TRANSACTION_CANNOT_BE_FINALIZED);
        }

        List<NoteItem> noteItems = noteItemRepository.findByExchangeNote_ExchangeNoteId(exchangeNoteId);

        for (NoteItem noteItem : noteItems) {
            Product product = productRepository.findById(noteItem.getProduct().getProductId())
                    .orElseThrow(() -> new AppException(ErrorCode.PRODUCT_NOT_FOUND));

            // Kiểm tra số lượng tồn kho
            if (product.getQuantity() < noteItem.getQuantity()) {
                throw new AppException(ErrorCode.INSUFFICIENT_STOCK);
            }

            // Giảm số lượng trong kho
            product.setQuantity(product.getQuantity() - noteItem.getQuantity());
            productRepository.save(product);
            if(product.getQuantity() == 0)
            {
                product.setStatus(ProductStatus.outofstock);
            }

            // Cập nhật trạng thái của NoteItem
            noteItem.setStatus(NoteItemStatus.COMPLETED);
        }
        noteItemRepository.saveAll(noteItems);

        // Cập nhật trạng thái giao dịch
        exchangeNote.setStatus(StockExchangeStatus.finished);
        stockTransactionRepository.save(exchangeNote);

        log.info("Giao dịch {} đã được hoàn tất thành công.", exchangeNoteId);
        return stockTransactionMapper.toResponse(exchangeNote);
    }

    private void validateItems(List<TransactionItemRequest> itemRequests, Warehouse sourceWarehouse,
                               Warehouse destinationWarehouse, StockTransactionType transactionType) {
        log.info("Kiểm tra các mục trước khi tạo giao dịch");

        for (TransactionItemRequest itemRequest : itemRequests) {
            var product = productRepository.findByProductCode(itemRequest.getProductCode())
                    .orElseThrow(() -> new AppException(ErrorCode.PRODUCT_NOT_FOUND));

            int requestedQuantity = itemRequest.getQuantity();
            log.info("Kiểm tra sản phẩm: {}, Số lượng yêu cầu: {}", product.getProductCode(), requestedQuantity);

            Warehouse warehouse = (transactionType == StockTransactionType.IMPORT)
                    ? destinationWarehouse
                    : sourceWarehouse;

            if (transactionType == StockTransactionType.EXPORT) {
                String warehouseCode = warehouse.getWarehouseCode();
                String productCode = product.getProductCode();

                // Lấy tổng nhập và tổng xuất (tương tự createStockCheckProduct)
                Integer totalImport = Optional.ofNullable(noteItemRepository.getTotalImportByProductCodeAndWarehouse(
                        productCode, warehouseCode)).orElse(0);
                Integer totalExport = Optional.ofNullable(noteItemRepository.getTotalExportByProductCodeAndWarehouse(
                        productCode, warehouseCode)).orElse(0);

                // Tính tồn kho lý thuyết
                int theoreticalStock = totalImport - totalExport;
                log.info("Tổng nhập: {}, Tổng xuất: {}, Tồn kho lý thuyết của {}: {}",
                        totalImport, totalExport, productCode, theoreticalStock);

                // Kiểm tra tồn kho
                if (theoreticalStock < requestedQuantity) {
                    throw new AppException(ErrorCode.INSUFFICIENT_STOCK);
                }
            }
        }
    }

    private List<NoteItem> createOrUpdateNoteItems(
            List<TransactionItemRequest> itemRequests,
            ExchangeNote exchangeNote,
            Warehouse sourceWarehouse,
            Warehouse destinationWarehouse,
            StockTransactionType transactionType) {

        log.info("Vào hàm createOrUpdate");
        List<NoteItem> noteItems = new ArrayList<>();

        for (TransactionItemRequest itemRequest : itemRequests) {
            var product = productRepository.findByProductCode(itemRequest.getProductCode())
                    .orElseThrow(() -> new AppException(ErrorCode.PRODUCT_NOT_FOUND));

            int quantity = itemRequest.getQuantity();
            log.info("Xử lý sản phẩm: {}, Số lượng: {}", product.getProductCode(), quantity);

            log.info("exchangeNote ID: {}", exchangeNote.getExchangeNoteId());
            log.info("product Code: {}", product.getProductCode());

            Optional<NoteItem> optionalNoteItem = noteItemRepository.findByExchangeNoteAndProduct(
                    exchangeNote, product);
            log.info("optional note item: {}", optionalNoteItem);

            if (optionalNoteItem.isPresent()) {
                NoteItem existingNoteItem = optionalNoteItem.get();
                log.info("Sản phẩm đã tồn tại trong ExchangeNote, cập nhật số lượng");
                if (transactionType == StockTransactionType.EXPORT) {
                    existingNoteItem.setQuantity(existingNoteItem.getQuantity() - quantity);
                } else {
                    existingNoteItem.setQuantity(existingNoteItem.getQuantity() + quantity);
                }
                noteItemRepository.save(existingNoteItem);
                noteItems.add(existingNoteItem);
            } else {
                log.info("Sản phẩm chưa có trong ExchangeNote, tạo mới");
                NoteItem newNoteItem = new NoteItem();
                newNoteItem.setNoteItemId(UUID.randomUUID().toString());
                newNoteItem.setNoteItemCode(UUID.randomUUID().toString().substring(0, 6));
                newNoteItem.setExchangeNote(exchangeNote);
                newNoteItem.setProduct(product);
                newNoteItem.setQuantity(quantity);
                newNoteItem.setStatus(NoteItemStatus.ACTIVE);
                noteItems.add(newNoteItem);
            }
        }

        return noteItems;
    }

    private StockExchangeResponse buildExchangeResponse(ExchangeNote exchangeNote) {
        StockExchangeResponse response = stockTransactionMapper.toResponse(exchangeNote);

        List<NoteItem> activeNoteItems = noteItemRepository.findByExchangeNoteAndStatus(
                exchangeNote, NoteItemStatus.ACTIVE);

        if (!activeNoteItems.isEmpty()) {
            List<NoteItemResponse> noteItemResponses = activeNoteItems.stream()
                    .map(noteItemMapper::toResponse)
                    .collect(Collectors.toList());
            response.setItems(noteItemResponses);
        }

        return response;
    }
}