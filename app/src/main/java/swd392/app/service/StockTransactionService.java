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
import swd392.app.dto.response.StockExchangeResponse;
import swd392.app.dto.response.NoteItemResponse;
import swd392.app.entity.ExchangeNote;
import swd392.app.entity.NoteItem;
import swd392.app.entity.User;
import swd392.app.entity.Warehouse;
import swd392.app.enums.StockExchangeStatus;
import swd392.app.enums.StockTransactionType;
import swd392.app.exception.AppException;
import swd392.app.exception.ErrorCode;
import swd392.app.mapper.StockTransactionMapper;
import swd392.app.mapper.NoteItemMapper;
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
public class StockTransactionService {
    StockTransactionRepository stockTransactionRepository;
    WarehouseRepository warehouseRepository;
    StockTransactionMapper stockTransactionMapper;
    NoteItemMapper noteItemMapper;
    UserRepository userRepository;
    ProductRepository productRepository;
    NoteItemRepository noteItemRepository;

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
        } else {
            sourceWarehouse = null; // Cho phép null khi là IMPORT
        }


        // Kiểm tra kho đích (chỉ cần nếu không phải xuất ra ngoài)
        if (request.getTransactionType() != StockTransactionType.EXPORT) {
            destinationWarehouse = warehouseRepository.findById(request.getDestinationWarehouseId())
                    .orElseThrow(() -> new AppException(ErrorCode.WAREHOUSE_NOT_FOUND));
            log.info("Kho đích: {}", destinationWarehouse.getWarehouseName());
        }

//        // Kiểm tra người tạo giao dịch
//        var createdByUser = userRepository.findByUserCode(request.getCreatedBy())
//                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXIST));
        // Lấy thông tin người dùng hiện tại
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null) {
            throw new AppException(ErrorCode.UNAUTHENTICATED);
        }

        String userName = authentication.getName();
        User checker = userRepository.findByEmail(userName)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXIST));

//        log.info("Người tạo giao dịch: {}", findByEmail.getFullName());

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

        // Lưu giao dịch trước khi xử lý sản phẩm
        ExchangeNote savedTransaction = stockTransactionRepository.save(transaction);

        // Xử lý danh sách sản phẩm
        List<NoteItem> noteItems = request.getItems().stream().map(item -> {
            var product = productRepository.findByProductCode(item.getProductCode())
                    .orElseThrow(() -> new AppException(ErrorCode.PRODUCT_NOT_FOUND));

            Optional<NoteItem> existingNoteItemOpt = noteItemRepository
                    .findByProduct_ProductCodeAndExchangeNote_DestinationWarehouse_WarehouseId(
                            item.getProductCode(), request.getDestinationWarehouseId());

            if (existingNoteItemOpt.isPresent()) {
                // Sản phẩm đã tồn tại, cộng dồn số lượng
                NoteItem existingNoteItem = existingNoteItemOpt.get();
                existingNoteItem.setQuantity(existingNoteItem.getQuantity() + item.getQuantity());
                return existingNoteItem;
            } else {
                // Sản phẩm chưa tồn tại, thêm mới
                NoteItem noteItem = new NoteItem();
                noteItem.setNoteItemId(UUID.randomUUID().toString());
                noteItem.setNoteItemCode(UUID.randomUUID().toString().substring(0, 6));
                noteItem.setExchangeNote(savedTransaction);
                noteItem.setProduct(product);
                noteItem.setQuantity(item.getQuantity());
                return noteItem;
            }

        }).collect(Collectors.toList());


        noteItems = noteItemRepository.saveAll(noteItems);
        savedTransaction.setNoteItems(noteItems);

        List<NoteItemResponse> noteItemResponses = noteItems.stream()
                .map(noteItemMapper::toResponse)
                .collect(Collectors.toList());

        StockExchangeResponse response = stockTransactionMapper.toResponse(savedTransaction);
        response.setItems(noteItemResponses);

        return response;
    }

    @PreAuthorize("hasRole('STAFF')")
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
            List<NoteItem> noteItems = noteItemRepository.findByExchangeNote_ExchangeNoteId(exchangeNoteId);
            exchangeNote.setNoteItems(noteItems);
        }

        log.info("Giao dịch đã được duyệt thành công.");
        return buildExchangeResponse(exchangeNote);
    }

    @PreAuthorize("hasRole('STAFF')")
    public StockExchangeResponse finalizeTransaction(String exchangeNoteId, boolean isFinished, boolean includeItems) {
        log.info("Hoàn tất giao dịch với mã: {}", exchangeNoteId);

        // Tìm giao dịch cần hoàn tất
        ExchangeNote exchangeNote = stockTransactionRepository.findById(exchangeNoteId)
                .orElseThrow(() -> new AppException(ErrorCode.TRANSACTION_NOT_FOUND));

        // Kiểm tra trạng thái hiện tại
        if (exchangeNote.getStatus() != StockExchangeStatus.accepted) {
            throw new AppException(ErrorCode.TRANSACTION_CANNOT_BE_FINALIZED);
        }

        // Cập nhật trạng thái giao dịch
        exchangeNote.setStatus(isFinished ? StockExchangeStatus.finished : StockExchangeStatus.rejected);
        stockTransactionRepository.save(exchangeNote);

        // Load đầy đủ danh sách items nếu includeItems = true
        if (includeItems) {
            List<NoteItem> noteItems = noteItemRepository.findByExchangeNote_ExchangeNoteId(exchangeNoteId);
            exchangeNote.setNoteItems(noteItems);
        }

        log.info("Giao dịch hoàn tất với trạng thái: {}", exchangeNote.getStatus());
        return buildExchangeResponse(exchangeNote);
    }

    // Phương thức để build dữ liệu phản hồi (tái sử dụng tránh lặp code)
    private StockExchangeResponse buildExchangeResponse(ExchangeNote exchangeNote) {
        List<NoteItemResponse> noteItemResponses = exchangeNote.getNoteItems().stream()
                .map(noteItemMapper::toResponse)
                .collect(Collectors.toList());

        StockExchangeResponse response = stockTransactionMapper.toResponse(exchangeNote);
        response.setItems(noteItemResponses);

        return response;
    }

    @PreAuthorize("hasRole('STAFF')")
    public List<StockExchangeResponse> getAllTransactions() {
        log.info("Lấy danh sách tất cả giao dịch");

        List<ExchangeNote> transactions = stockTransactionRepository.findAll();
        return transactions.stream()
                .map(stockTransactionMapper::toResponse)
                .collect(Collectors.toList());
    }

    @PreAuthorize("hasRole('STAFF')")
    public List<StockExchangeResponse> getTransactionsByWarehouse(String warehouseCode) {
        log.info("Lấy giao dịch theo kho: {}", warehouseCode);

        List<ExchangeNote> transactions = stockTransactionRepository.findByDestinationWarehouse_WarehouseCode(warehouseCode);
        return transactions.stream()
                .map(stockTransactionMapper::toResponse)
                .collect(Collectors.toList());
    }

}
