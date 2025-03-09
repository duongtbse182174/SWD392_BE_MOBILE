package swd392.app.service;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import swd392.app.dto.request.StockExchangeRequest;
import swd392.app.dto.request.TransactionItemRequest;
import swd392.app.dto.response.StockExchangeResponse;
import swd392.app.dto.response.NoteItemResponse;
import swd392.app.entity.ExchangeNote;
import swd392.app.entity.NoteItem;
import swd392.app.entity.User;
import swd392.app.entity.Warehouse;
import swd392.app.enums.StockExchangeStatus;
import swd392.app.enums.StockTransactionType;
import swd392.app.enums.SourceType;
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

    // Bản đồ để lưu trữ NoteItem tạm thời
    Map<String, List<NoteItem>> temporaryNoteItems = new HashMap<>();

    @PreAuthorize("hasRole('STAFF')")
    public StockExchangeResponse createTransaction(StockExchangeRequest request) {
        log.info("Bắt đầu tạo giao dịch: {}", request);

        Warehouse sourceWarehouse = null;
        Warehouse destinationWarehouse = null;

        // Kiểm tra kho nguồn (chỉ cần nếu không phải nhập từ ngoài)
        if (request.getTransactionType() != StockTransactionType.IMPORT) {
            if (request.getSourceWarehouseId() == null) {
                throw new AppException(ErrorCode.WAREHOUSE_REQUIRED);
            }
            sourceWarehouse = warehouseRepository.findById(request.getSourceWarehouseId())
                    .orElseThrow(() -> new AppException(ErrorCode.WAREHOUSE_NOT_FOUND));
        }

        // Kiểm tra kho đích (chỉ cần nếu không phải xuất ra ngoài)
        if (request.getTransactionType() != StockTransactionType.EXPORT) {
            destinationWarehouse = warehouseRepository.findById(request.getDestinationWarehouseId())
                    .orElseThrow(() -> new AppException(ErrorCode.WAREHOUSE_NOT_FOUND));
            log.info("Kho đích: {}", destinationWarehouse.getWarehouseName());
        }

        // Xác định người dùng hiện tại
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null) {
            throw new AppException(ErrorCode.UNAUTHENTICATED);
        }

        String userName = authentication.getName();
        User checker = userRepository.findByEmail(userName)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXIST));

        // Tạo giao dịch mới
        ExchangeNote transaction = new ExchangeNote();
        transaction.setExchangeNoteId(UUID.randomUUID().toString());
        transaction.setDate(LocalDate.now());
        transaction.setTransactionType(request.getTransactionType());
        transaction.setSourceType(request.getSourceType());
        transaction.setSourceWarehouse(sourceWarehouse);
        transaction.setDestinationWarehouse(destinationWarehouse);
        transaction.setCreatedBy(checker);
        transaction.setStatus(StockExchangeStatus.pending);

        // Lưu giao dịch vào database (không bao gồm NoteItems)
        ExchangeNote savedTransaction = stockTransactionRepository.save(transaction);

        // Tạo các đối tượng NoteItem từ request nhưng không lưu vào database
        List<NoteItem> noteItems = createOrUpdateNoteItems(request.getItems(), savedTransaction, sourceWarehouse, destinationWarehouse, request.getTransactionType());

        // Lưu trữ NoteItem tạm thời vào bản đồ
        temporaryNoteItems.put(savedTransaction.getExchangeNoteId(), noteItems);

        // Gán danh sách noteItems cho transaction
        transaction.setNoteItems(noteItems);

        // Chuyển đổi dữ liệu thành response
        List<NoteItemResponse> noteItemResponses = noteItems.stream()
                .map(noteItemMapper::toResponse)
                .collect(Collectors.toList());

        StockExchangeResponse response = stockTransactionMapper.toResponse(transaction);
        response.setItems(noteItemResponses);

        return response;
    }

    @PreAuthorize("hasRole('MANAGER')")
    public StockExchangeResponse approveTransaction(String exchangeNoteId, boolean includeItems) {
        log.info("Duyệt giao dịch với mã: {}", exchangeNoteId);

        ExchangeNote exchangeNote = stockTransactionRepository.findById(exchangeNoteId)
                .orElseThrow(() -> new AppException(ErrorCode.TRANSACTION_NOT_FOUND));

        if (exchangeNote.getStatus() != StockExchangeStatus.pending) {
            throw new AppException(ErrorCode.TRANSACTION_CANNOT_BE_MODIFIED);
        }

        exchangeNote.setStatus(StockExchangeStatus.accepted);
        stockTransactionRepository.save(exchangeNote);

        if (includeItems) {
            List<NoteItem> noteItems = temporaryNoteItems.get(exchangeNoteId);
            exchangeNote.setNoteItems(noteItems);
        }

        log.info("Giao dịch đã được duyệt thành công.");
        return buildExchangeResponse(exchangeNote);
    }

    @PreAuthorize("hasRole('MANAGER')")
    public StockExchangeResponse finalizeTransaction(String exchangeNoteId, boolean isFinished, boolean includeItems) {
        log.info("Hoàn tất giao dịch với mã: {}", exchangeNoteId);

        // Tìm giao dịch cần hoàn tất
        ExchangeNote exchangeNote = stockTransactionRepository.findById(exchangeNoteId)
                .orElseThrow(() -> new AppException(ErrorCode.TRANSACTION_NOT_FOUND));

        // Kiểm tra trạng thái hiện tại
        if (exchangeNote.getStatus() != StockExchangeStatus.accepted) {
            throw new AppException(ErrorCode.TRANSACTION_CANNOT_BE_FINALIZED);
        }

        // Lấy danh sách các NoteItem từ bản đồ tạm thời
        List<NoteItem> noteItems = temporaryNoteItems.get(exchangeNoteId);

        if (noteItems == null || noteItems.isEmpty()) {
            throw new AppException(ErrorCode.NOTE_ITEMS_NOT_FOUND);
        }

        // Nếu trạng thái là finished, cộng dồn lượng sản phẩm cho NoteItem và lưu vào database
        if (isFinished) {
            Map<String, NoteItem> aggregatedNoteItems = new HashMap<>();

            for (NoteItem noteItem : noteItems) {
                String key = noteItem.getProduct().getProductCode() + "-" + noteItem.getWarehouse().getWarehouseCode();
                if (aggregatedNoteItems.containsKey(key)) {
                    NoteItem existingNoteItem = aggregatedNoteItems.get(key);
                    existingNoteItem.setQuantity(existingNoteItem.getQuantity() + noteItem.getQuantity());
                } else {
                    aggregatedNoteItems.put(key, noteItem);
                }
            }

            List<NoteItem> finalNoteItems = new ArrayList<>(aggregatedNoteItems.values());
            finalNoteItems = noteItemRepository.saveAll(finalNoteItems);
            exchangeNote.setNoteItems(finalNoteItems);
            exchangeNote.setStatus(StockExchangeStatus.finished);

            // Xóa các NoteItem đã lưu vào cơ sở dữ liệu khỏi bản đồ tạm thời
            temporaryNoteItems.remove(exchangeNoteId);
        } else {
            // Nếu rejected, xóa tất cả NoteItems tạm thời
            temporaryNoteItems.remove(exchangeNoteId);
            exchangeNote.setNoteItems(new ArrayList<>());
            exchangeNote.setStatus(StockExchangeStatus.rejected);
        }

        // Lưu trạng thái mới của giao dịch
        stockTransactionRepository.save(exchangeNote);

        // Nếu includeItems=true và là finished, gán noteItems vào response
        if (includeItems && isFinished) {
            exchangeNote.setNoteItems(noteItems);
        }

        // Chuyển đổi dữ liệu thành response
        StockExchangeResponse response = stockTransactionMapper.toResponse(exchangeNote);

        if (isFinished && !noteItems.isEmpty()) {
            List<NoteItemResponse> noteItemResponses = noteItems.stream()
                    .map(noteItemMapper::toResponse)
                    .collect(Collectors.toList());
            response.setItems(noteItemResponses);
        }

        log.info("Giao dịch hoàn tất với trạng thái: {}", exchangeNote.getStatus());
        return response;
    }

    /**
     * Phương thức để tạo hoặc cập nhật NoteItem từ danh sách request
     */
    private List<NoteItem> createOrUpdateNoteItems(
            List<TransactionItemRequest> itemRequests,
            ExchangeNote exchangeNote,
            Warehouse sourceWarehouse,
            Warehouse destinationWarehouse,
            StockTransactionType transactionType) {

        List<NoteItem> noteItems = new ArrayList<>();

        for (TransactionItemRequest itemRequest : itemRequests) {
            var product = productRepository.findByProductCode(itemRequest.getProductCode())
                    .orElseThrow(() -> new AppException(ErrorCode.PRODUCT_NOT_FOUND));

            Warehouse warehouse = null;
            int quantity = itemRequest.getQuantity();

            switch (transactionType) {
                case IMPORT:
                    if (exchangeNote.getSourceType() == SourceType.EXTERNAL) {
                        warehouse = destinationWarehouse;
                    } else if (exchangeNote.getSourceType() == SourceType.INTERNAL) {
                        if (product == null) {
                            throw new AppException(ErrorCode.PRODUCT_NOT_FOUND);
                        }
                        warehouse = destinationWarehouse;
                    } else {
                        throw new AppException(ErrorCode.INVALID_SOURCE_TYPE);
                    }
                    break;

                case EXPORT:
                    if (exchangeNote.getSourceType() != SourceType.EXTERNAL) {
                        throw new AppException(ErrorCode.INVALID_SOURCE_TYPE);
                    }
                    warehouse = sourceWarehouse;
                    Optional<NoteItem> sourceNoteItemOptional = noteItemRepository.findByProduct_ProductCodeAndWarehouse_WarehouseCode(
                            itemRequest.getProductCode(), sourceWarehouse.getWarehouseCode());
                    if (sourceNoteItemOptional.isPresent()) {
                        NoteItem sourceNoteItem = sourceNoteItemOptional.get();
                        if (sourceNoteItem.getQuantity() < itemRequest.getQuantity()) {
                            throw new AppException(ErrorCode.INSUFFICIENT_STOCK);
                        }
                        quantity = -quantity;
                    } else {
                        throw new AppException(ErrorCode.NOTE_ITEMS_NOT_FOUND);
                    }
                    break;

                case TRANSFER:
                    Optional<NoteItem> transferSourceNoteItemOptional = noteItemRepository.findByProduct_ProductCodeAndWarehouse_WarehouseCode(
                            itemRequest.getProductCode(), sourceWarehouse.getWarehouseCode());
                    if (transferSourceNoteItemOptional.isPresent()) {
                        NoteItem sourceNoteItem = transferSourceNoteItemOptional.get();
                        if (sourceNoteItem.getQuantity() < itemRequest.getQuantity()) {
                            throw new AppException(ErrorCode.INSUFFICIENT_STOCK);
                        }
                        sourceNoteItem.setQuantity(sourceNoteItem.getQuantity() - itemRequest.getQuantity());
                        sourceNoteItem.setExchangeNote(exchangeNote);
                        noteItems.add(sourceNoteItem);
                    } else {
                        throw new AppException(ErrorCode.NOTE_ITEMS_NOT_FOUND);
                    }

                    warehouse = destinationWarehouse;
                    break;

                default:
                    throw new AppException(ErrorCode.INVALID_TRANSACTION_TYPE);
            }

            Optional<NoteItem> optionalNoteItem = noteItemRepository.findByProduct_ProductCodeAndWarehouse_WarehouseCode(
                    itemRequest.getProductCode(), warehouse.getWarehouseCode());

            if (optionalNoteItem.isPresent()) {
                NoteItem existingNoteItem = optionalNoteItem.get();
                existingNoteItem.setQuantity(existingNoteItem.getQuantity() + quantity);
                existingNoteItem.setExchangeNote(exchangeNote);
                noteItems.add(existingNoteItem);
            } else {
                NoteItem newNoteItem = new NoteItem();
                newNoteItem.setNoteItemId(UUID.randomUUID().toString());
                newNoteItem.setNoteItemCode(UUID.randomUUID().toString().substring(0, 6));
                newNoteItem.setExchangeNote(exchangeNote);
                newNoteItem.setProduct(product);
                newNoteItem.setWarehouse(warehouse);
                newNoteItem.setQuantity(quantity);
                noteItems.add(newNoteItem);
            }
        }

        return noteItems;
    }


    /**
     * Phương thức buildExchangeResponse để tạo response đầy đủ
     */
    private StockExchangeResponse buildExchangeResponse(ExchangeNote exchangeNote) {
        StockExchangeResponse response = stockTransactionMapper.toResponse(exchangeNote);

        if (exchangeNote.getNoteItems() != null && !exchangeNote.getNoteItems().isEmpty()) {
            List<NoteItemResponse> noteItemResponses = exchangeNote.getNoteItems().stream()
                    .map(noteItemMapper::toResponse)
                    .collect(Collectors.toList());
            response.setItems(noteItemResponses);
        }

        return response;
    }
}
