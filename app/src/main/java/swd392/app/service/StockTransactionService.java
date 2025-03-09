package swd392.app.service;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
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
            sourceWarehouse = warehouseRepository.findById(request.getSourceWarehouseId())
                    .orElseThrow(() -> new AppException(ErrorCode.WAREHOUSE_NOT_FOUND));
            log.info("Kho nguồn: {}", sourceWarehouse.getWarehouseName());
        }

        // Kiểm tra kho đích (chỉ cần nếu không phải xuất ra ngoài)
        if (request.getTransactionType() != StockTransactionType.EXPORT) {
            destinationWarehouse = warehouseRepository.findById(request.getDestinationWarehouseId())
                    .orElseThrow(() -> new AppException(ErrorCode.WAREHOUSE_NOT_FOUND));
            log.info("Kho đích: {}", destinationWarehouse.getWarehouseName());
        }

        // Kiểm tra người tạo giao dịch
        var createdByUser = userRepository.findByUserCode(request.getCreatedBy())
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXIST));

        log.info("Người tạo giao dịch: {}", createdByUser.getFullName());

        // Tạo giao dịch mới
        ExchangeNote transaction = new ExchangeNote();
        transaction.setExchangeNoteId(UUID.randomUUID().toString());
        transaction.setDate(LocalDate.now());
        transaction.setTransactionType(request.getTransactionType());
        transaction.setSourceWarehouse(sourceWarehouse);
        transaction.setDestinationWarehouse(destinationWarehouse);
        transaction.setCreatedBy(createdByUser);
        transaction.setStatus(StockExchangeStatus.pending);

        // Lưu giao dịch trước khi xử lý sản phẩm
        ExchangeNote savedTransaction = stockTransactionRepository.save(transaction);

        // Xử lý danh sách sản phẩm
        List<NoteItem> noteItems = request.getItems().stream().map(item -> {
            var product = productRepository.findByProductCode(item.getProductCode())
                    .orElseThrow(() -> new AppException(ErrorCode.PRODUCT_NOT_FOUND));

            NoteItem noteItem = new NoteItem();
            noteItem.setNoteItemId(UUID.randomUUID().toString());
            noteItem.setNoteItemCode(UUID.randomUUID().toString().substring(0, 6));
            noteItem.setExchangeNote(savedTransaction);
            noteItem.setProduct(product);
            noteItem.setQuantity(item.getQuantity());

            return noteItem;
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
}
