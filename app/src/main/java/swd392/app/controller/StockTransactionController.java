package swd392.app.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import swd392.app.dto.request.StockExchangeRequest;
import swd392.app.dto.response.ApiResponse;
import swd392.app.dto.response.StockExchangeResponse;
import swd392.app.service.StockTransactionService;

@RestController
@RequestMapping("/transactions")
@RequiredArgsConstructor
public class StockTransactionController {
    private final StockTransactionService stockTransactionService;

    @PostMapping("/create")
    public ApiResponse<StockExchangeResponse> createTransaction(@Valid @RequestBody StockExchangeRequest request) {
        return ApiResponse.<StockExchangeResponse>builder()
                .result(stockTransactionService.createTransaction(request))
                .build();
    }
}
