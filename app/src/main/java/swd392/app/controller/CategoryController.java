package swd392.app.controller;

import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import swd392.app.dto.request.CategoryRequest;
import swd392.app.dto.response.ApiResponse;
import swd392.app.dto.response.CategoryResponse;
import swd392.app.service.CategoryService;

import java.util.List;

@RestController
@RequestMapping("/categories")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class CategoryController {

    CategoryService categoryService;

    @PostMapping("/create")
    ApiResponse<CategoryResponse> createCategory(@RequestBody @Valid CategoryRequest request) {
        return ApiResponse.<CategoryResponse>builder()
                .result(categoryService.createCategory(request))
                .build();
    }

    @GetMapping("/{categoryCode}")
    ApiResponse<CategoryResponse> getCategory(@PathVariable String categoryCode) {
        return ApiResponse.<CategoryResponse>builder()
                .result(categoryService.getCategoryByCode(categoryCode))
                .build();
    }

    @GetMapping
    ApiResponse<List<CategoryResponse>> getAllCategories() {
        return ApiResponse.<List<CategoryResponse>>builder()
                .result(categoryService.getAllCategories())
                .build();
    }

    @PutMapping("/{categoryCode}")
    ApiResponse<CategoryResponse> updateCategory(@PathVariable String categoryCode,
                                                 @RequestBody @Valid CategoryRequest request) {
        return ApiResponse.<CategoryResponse>builder()
                .result(categoryService.updateCategory(categoryCode, request))
                .build();
    }

    @DeleteMapping("/{categoryCode}")
    ApiResponse<Void> deleteCategory(@PathVariable String categoryCode) {
        categoryService.deleteCategory(categoryCode);
        return ApiResponse.<Void>builder().build();
    }
}
