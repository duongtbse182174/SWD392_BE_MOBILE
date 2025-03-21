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
import swd392.app.enums.NoteItemStatus;
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

    @PreAuthorize("hasRole('STAFF')")
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

        User checker = userRepository.findByEmail(authentication.getName())
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXIST));

        ExchangeNote transaction = new ExchangeNote();
        transaction.setExchangeNoteId(UUID.randomUUID().toString());
        transaction.setDate(LocalDate.now());
        transaction.setTransactionType(request.getTransactionType());
        transaction.setSourceWarehouse(sourceWarehouse);
        transaction.setDestinationWarehouse(destinationWarehouse);
        transaction.setCreatedBy(checker);
        transaction.setStatus(StockExchangeStatus.pending);

        ExchangeNote savedTransaction = stockTransactionRepository.save(transaction);

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

        exchangeNote.setStatus(StockExchangeStatus.accepted);
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
            noteItem.setStatus(NoteItemStatus.COMPLETED);
        }
        noteItemRepository.saveAll(noteItems);

        exchangeNote.setStatus(StockExchangeStatus.finished);
        stockTransactionRepository.save(exchangeNote);

        log.info("Giao dịch {} đã được hoàn tất thành công.", exchangeNoteId);
        return stockTransactionMapper.toResponse(exchangeNote);
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
            Warehouse warehouse = (transactionType == StockTransactionType.IMPORT)
                    ? destinationWarehouse
                    : sourceWarehouse;

            Optional<NoteItem> optionalNoteItem = noteItemRepository.findByExchangeNoteAndProduct(
                    exchangeNote, product);

            if (optionalNoteItem.isPresent()) {
                NoteItem existingNoteItem = optionalNoteItem.get();
                existingNoteItem.setQuantity(existingNoteItem.getQuantity() + quantity);
            } else {
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