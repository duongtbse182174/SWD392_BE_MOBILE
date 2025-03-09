package swd392.app.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import swd392.app.dto.request.StockExchangeRequest;
import swd392.app.dto.response.ApiResponse;
import swd392.app.dto.response.StockExchangeResponse;
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
    @PostMapping("/approve/{id}")
    public ApiResponse<StockExchangeResponse> approveTransaction(
            @PathVariable String id,
            @RequestParam(required = false, defaultValue = "true") boolean includeItems
    ) {
        return ApiResponse.<StockExchangeResponse>builder()
                .result(stockTransactionService.approveTransaction(id, includeItems))
                .build();
    }

    // Hoàn tất giao dịch (bổ sung tham số includeItems)
    @PostMapping("/finalize/{id}")
    public ApiResponse<StockExchangeResponse> finalizeTransaction(
            @PathVariable String id,
            @RequestParam boolean isFinished,
            @RequestParam(required = false, defaultValue = "true") boolean includeItems
    ) {
        return ApiResponse.<StockExchangeResponse>builder()
                .result(stockTransactionService.finalizeTransaction(id, isFinished, includeItems))
                .build();
    }

//    // Lấy danh sách tất cả giao dịch
//    @GetMapping("/all")
//    public ApiResponse<List<StockExchangeResponse>> getAllTransactions() {
//        return ApiResponse.<List<StockExchangeResponse>>builder()
//                .result(stockTransactionService.getAllTransactions())
//                .build();
//    }
//
//    // Lấy danh sách giao dịch theo mã kho
//    @GetMapping("/warehouse/{warehouseCode}")
//    public ApiResponse<List<StockExchangeResponse>> getTransactionsByWarehouse(@PathVariable String warehouseCode) {
//        return ApiResponse.<List<StockExchangeResponse>>builder()
//                .result(stockTransactionService.getTransactionsByWarehouse(warehouseCode))
//                .build();
//    }
}
