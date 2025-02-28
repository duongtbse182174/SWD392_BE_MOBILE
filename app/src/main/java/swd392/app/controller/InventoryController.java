package swd392.app.controller;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import swd392.app.dto.response.ApiResponse;
import swd392.app.dto.response.StockResponse;
import swd392.app.dto.response.UserResponse;
import swd392.app.service.InventoryService;

import java.util.List;

@RestController
@RequestMapping("/inventory")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class InventoryController {
    InventoryService inventoryService;

    @GetMapping("/stocks")
    public ApiResponse<List<StockResponse>> getAllStocks() {
        List<StockResponse> stocks = inventoryService.getAllStocks();
        return ApiResponse.<List<StockResponse>>builder()
                .message("Success")
                .result(stocks)
                .build();
    }

    @GetMapping("/stock/{productCode}")
    public ApiResponse<StockResponse> getStockByProductCode(@PathVariable String productCode) {
        StockResponse stock = inventoryService.getStockByProductCode(productCode);
        return ApiResponse.<StockResponse>builder()
                .message("Success")
                .result(stock)
                .build();
    }

}