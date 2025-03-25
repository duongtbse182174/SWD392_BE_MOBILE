package swd392.app.controller;

import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import swd392.app.dto.request.StockCheckNoteRequest;
import swd392.app.dto.response.ApiResponse;
import swd392.app.dto.response.StockCheckNoteResponse;
import swd392.app.service.StockCheckService;

import java.util.List;

@RestController
@RequestMapping("/stock-check")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class StockCheckController {
    StockCheckService stockCheckService;

    // Tạo mới phiếu kiểm kho
    @PostMapping("/create")
    public ApiResponse<StockCheckNoteResponse> createStockCheckNote(
            @Valid @RequestBody StockCheckNoteRequest request) {
        return ApiResponse.<StockCheckNoteResponse>builder()
                .result(stockCheckService.createStockCheckNote(request))
                .build();
    }

    @GetMapping()
    public ApiResponse<List<StockCheckNoteResponse>> getAllStockCheckNotes() {
        return ApiResponse.<List<StockCheckNoteResponse>>builder()
                .result(stockCheckService.getAllStockCheckNotes())
                .build();
    }

    @GetMapping("/warehouse/{warehouseCode}")
    public ApiResponse<List<StockCheckNoteResponse>> getStockCheckNotesByWarehouse(
            @PathVariable String warehouseCode) {
        return ApiResponse.<List<StockCheckNoteResponse>>builder()
                .result(stockCheckService.getStockCheckNotesByWarehouse(warehouseCode))
                .build();
    }

    // Duyệt phiếu kiểm kho
    @PostMapping("/approve/{id}")
    public ApiResponse<StockCheckNoteResponse> approveStockCheck(
            @PathVariable String id
    ) {
        return ApiResponse.<StockCheckNoteResponse>builder()
                .result(stockCheckService.approveStockCheck(id))
                .build();
    }

    // Hoàn tất phiếu kiểm kho
    @PostMapping("/finalize/{id}")
    public ApiResponse<StockCheckNoteResponse> finalizeStockCheck(
            @PathVariable String id,
            @RequestParam boolean isFinished
    ) {
        return ApiResponse.<StockCheckNoteResponse>builder()
                .result(stockCheckService.finalizeStockCheck(id, isFinished))
                .build();
    }

    @GetMapping("/filter")
    public ApiResponse<List<StockCheckNoteResponse>> getStockCheckNotesByStatus(
            @RequestParam(required = false) String status) {
        List<StockCheckNoteResponse> result;

        // Nếu không có status được cung cấp, trả về tất cả phiếu kiểm kho
        if (status == null || status.trim().isEmpty()) {
            result = stockCheckService.getAllStockCheckNotes();
        } else {
            result = stockCheckService.getStockCheckNotesByStatus(status);
        }

        return ApiResponse.<List<StockCheckNoteResponse>>builder()
                .result(result)
                .build();
    }
}
