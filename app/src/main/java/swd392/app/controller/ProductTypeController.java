package swd392.app.controller;

import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import swd392.app.dto.request.ProductTypeRequest;
import swd392.app.dto.response.ApiResponse;
import swd392.app.dto.response.ProductTypeResponse;
import swd392.app.service.ProductTypeService;

import java.util.List;

@RestController
@RequestMapping("/productTypes")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class ProductTypeController {

    ProductTypeService productTypeService;

    @PostMapping("/create")
    ApiResponse<ProductTypeResponse> createProductType(@RequestBody @Valid ProductTypeRequest request) {
        return ApiResponse.<ProductTypeResponse>builder()
                .result(productTypeService.createProductType(request))
                .build();
    }

    @GetMapping("/{productTypeCode}")
    ApiResponse<ProductTypeResponse> getProductType(@PathVariable String productTypeCode) {
        return ApiResponse.<ProductTypeResponse>builder()
                .result(productTypeService.getProductTypeByCode(productTypeCode))
                .build();
    }

    @GetMapping
    ApiResponse<List<ProductTypeResponse>> getAllProductTypes() {
        return ApiResponse.<List<ProductTypeResponse>>builder()
                .result(productTypeService.getAllProductTypes())
                .build();
    }

    @PutMapping("/{productTypeCode}")
    ApiResponse<ProductTypeResponse> updateProductType(@PathVariable String productTypeCode,
                                                       @RequestBody @Valid ProductTypeRequest request) {
        return ApiResponse.<ProductTypeResponse>builder()
                .result(productTypeService.updateProductType(productTypeCode, request))
                .build();
    }

    @DeleteMapping("/{productTypeCode}")
    ApiResponse<Void> deleteProductType(@PathVariable String productTypeCode) {
        productTypeService.deleteProductType(productTypeCode);
        return ApiResponse.<Void>builder().build();
    }
}
