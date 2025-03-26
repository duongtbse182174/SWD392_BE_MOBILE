package swd392.app.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import swd392.app.dto.request.StockExchangeRequest;
import swd392.app.dto.response.ApiResponse;
import swd392.app.dto.response.StockExchangeResponse;
import swd392.app.enums.StockTransactionType;
import swd392.app.service.StockTransactionService;

import java.util.List;

@RestController
@RequestMapping("/transactions")
@RequiredArgsConstructor
public class StockTransactionController {
    private final StockTransactionService stockTransactionService;

    // Tạo giao dịch mới
    @PostMapping("/create")
    public ApiResponse<StockExchangeResponse> createTransaction(@Valid @RequestBody StockExchangeRequest request) {
        return ApiResponse.<StockExchangeResponse>builder()
                .result(stockTransactionService.createTransaction(request))
                .build();
    }

    // Duyệt giao dịch (bổ sung tham số includeItems để quyết định có tải items hay không)
    @PostMapping("/approve/{exchangeNoteId}")
    public ApiResponse<StockExchangeResponse> approveTransaction(
            @PathVariable String exchangeNoteId
    ) {
        return ApiResponse.<StockExchangeResponse>builder()
                .result(stockTransactionService.approveTransaction(exchangeNoteId))
                .build();
    }

    @PostMapping("/finalize/{exchangeNoteId}")
    public ApiResponse<StockExchangeResponse> finalizeTransaction(
            @PathVariable String exchangeNoteId,
            @RequestParam(defaultValue = "false") boolean includeItems) {
        return ApiResponse.<StockExchangeResponse>builder()
                .result(stockTransactionService.finalizeTransaction(exchangeNoteId))
                .build();
    }

    @PostMapping("/cancel/{exchangeNoteId}")
    public ApiResponse<StockExchangeResponse> cancelTransaction(@PathVariable String exchangeNoteId) {
        return ApiResponse.<StockExchangeResponse>builder()
                .result(stockTransactionService.cancelTransaction(exchangeNoteId))
                .build();
    }

    @GetMapping("/all")
    public ApiResponse<List<StockExchangeResponse>> getAllTransactions() {
        return ApiResponse.<List<StockExchangeResponse>>builder()
                .result(stockTransactionService.getAllTransactions())
                .build();
    }

    @GetMapping("/warehouse/{warehouseCode}")
    public ApiResponse<List<StockExchangeResponse>> getTransactionsByWarehouse(@PathVariable String warehouseCode) {
        return ApiResponse.<List<StockExchangeResponse>>builder()
                .result(stockTransactionService.getTransactionsByWarehouse(warehouseCode))
                .build();
    }

    @GetMapping("/pending")
    public ApiResponse<List<StockExchangeResponse>> getPendingTransactions() {
        return ApiResponse.<List<StockExchangeResponse>>builder()
                .result(stockTransactionService.getPendingTransactions())
                .build();
    }

    @GetMapping("/{exchangeNoteId}")
    public ApiResponse<StockExchangeResponse> getTransactionById(
            @PathVariable String exchangeNoteId
    ) {
        return ApiResponse.<StockExchangeResponse>builder()
                .result(stockTransactionService.getTransactionById(exchangeNoteId))
                .build();
    }

}
