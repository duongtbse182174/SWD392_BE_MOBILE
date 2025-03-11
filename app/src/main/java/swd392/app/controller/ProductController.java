package swd392.app.controller;

import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import swd392.app.dto.request.ProductRequest;
import swd392.app.dto.response.ApiResponse;
import swd392.app.dto.response.ProductResponse;
import swd392.app.service.ProductService;

import java.util.List;

@RestController
@RequestMapping("/products")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class ProductController {

    ProductService productService;

    @PostMapping("/create")
    ApiResponse<ProductResponse> createProduct(@RequestBody @Valid ProductRequest request) {
        return ApiResponse.<ProductResponse>builder()
                .result(productService.createProduct(request))
                .build();
    }

    @GetMapping("/{productCode}")
    ApiResponse<ProductResponse> getProduct(@PathVariable String productCode) {
        return ApiResponse.<ProductResponse>builder()
                .result(productService.getProductByCode(productCode))
                .build();
    }

    @GetMapping
    ApiResponse<List<ProductResponse>> getAllProducts() {
        return ApiResponse.<List<ProductResponse>>builder()
                .result(productService.getAllProducts())
                .build();
    }

    @PutMapping("/{productCode}")
    ApiResponse<ProductResponse> updateProduct(@PathVariable String productCode,
                                               @RequestBody @Valid ProductRequest request) {
        return ApiResponse.<ProductResponse>builder()
                .result(productService.updateProduct(productCode, request))
                .build();
    }

    @PutMapping("/{productCode}/outofstock")
    ApiResponse<Void> markProductOutOfStock(@PathVariable String productCode) {
        productService.deleteProduct(productCode); // Giữ nguyên logic trong service
        return ApiResponse.<Void>builder().message("Product marked as out of stock successfully").build();
    }

}
