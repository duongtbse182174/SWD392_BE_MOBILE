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
import swd392.app.enums.StockExchangeStatus;
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

        // Kiểm tra kho nguồn
        var sourceWarehouse = warehouseRepository.findById(request.getSourceWarehouseId())
                .orElseThrow(() -> new AppException(ErrorCode.WAREHOUSE_NOT_FOUND));
        log.info("Kho nguồn: {}", sourceWarehouse.getWarehouseName());

        // Kiểm tra kho đích
        var destinationWarehouse = warehouseRepository.findById(request.getDestinationWarehouseId())
                .orElseThrow(() -> new AppException(ErrorCode.WAREHOUSE_NOT_FOUND));
        log.info("Kho đích: {}", destinationWarehouse.getWarehouseName());

        // Kiểm tra người tạo giao dịch
        var createdByUser = userRepository.findByUserCode(request.getCreatedBy())
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXIST));
        log.info("Người tạo giao dịch: {}", createdByUser.getFullName());

        // Tạo giao dịch mới
        ExchangeNote transaction = new ExchangeNote();
        transaction.setExchangeNoteId(UUID.randomUUID().toString()); // Hoặc logic sinh ID của bạn
        transaction.setDate(LocalDate.now());
        transaction.setTransactionType(request.getTransactionType());
        transaction.setSourceWarehouse(sourceWarehouse);
        transaction.setDestinationWarehouse(destinationWarehouse);
        transaction.setCreatedBy(createdByUser);
        transaction.setStatus(StockExchangeStatus.pending);

        log.info("Đang tạo giao dịch: type={}, sourceWarehouse={}, destinationWarehouse={}, createdBy={}, status={}",
                request.getTransactionType(), sourceWarehouse.getWarehouseName(),
                destinationWarehouse.getWarehouseName(), createdByUser.getFullName(), StockExchangeStatus.pending);
        log.info("Transaction trước khi lưu: type={}, sourceWarehouse={}, destinationWarehouse={}, createdBy={}, status={}",
                transaction.getTransactionType(),
                transaction.getSourceWarehouse() != null ? transaction.getSourceWarehouse().getWarehouseName() : "null",
                transaction.getDestinationWarehouse() != null ? transaction.getDestinationWarehouse().getWarehouseName() : "null",
                transaction.getCreatedBy() != null ? transaction.getCreatedBy().getFullName() : "null",
                transaction.getStatus()
        );
        log.info("Transaction hashCode trước khi lưu: {}", transaction.hashCode());
        // Lưu giao dịch trước khi xử lý sản phẩm
        ExchangeNote savedTransaction = stockTransactionRepository.save(transaction);
        log.info("Giao dịch đã được lưu với ID: {}", savedTransaction.getExchangeNoteId());

        // Xử lý danh sách sản phẩm
        List<NoteItem> noteItems = request.getItems().stream().map(item -> {
            var product = productRepository.findByProductCode(item.getProductCode())
                    .orElseThrow(() -> new AppException(ErrorCode.PRODUCT_NOT_FOUND));

            NoteItem noteItem = new NoteItem();
            noteItem.setNoteItemId(UUID.randomUUID().toString());
            noteItem.setNoteItemCode(UUID.randomUUID().toString().substring(0, 5));
            noteItem.setExchangeNote(savedTransaction); // Gán ExchangeNote đã lưu
            noteItem.setProduct(product);
            noteItem.setQuantity(item.getQuantity());

            return noteItem;
        }).collect(Collectors.toList());

        // Lưu danh sách sản phẩm trước khi gán vào transaction
        noteItems = noteItemRepository.saveAll(noteItems);
        log.info("Đã thêm {} sản phẩm vào giao dịch.", noteItems.size());
        savedTransaction.setNoteItems(noteItems);

        // Chuyển đổi danh sách NoteItem thành NoteItemResponse
        List<NoteItemResponse> noteItemResponses = noteItems.stream()
                .map(noteItemMapper::toResponse)
                .collect(Collectors.toList());

        // Chuyển đổi ExchangeNote thành response
        StockExchangeResponse response = stockTransactionMapper.toResponse(savedTransaction);
        response.setItems(noteItemResponses);

        return response;
    }
}