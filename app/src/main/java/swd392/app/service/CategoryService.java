package swd392.app.service;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import swd392.app.dto.request.CategoryRequest;
import swd392.app.dto.response.CategoryResponse;
import swd392.app.entity.Category;
import swd392.app.exception.AppException;
import swd392.app.exception.ErrorCode;
import swd392.app.mapper.CategoryMapper;
import swd392.app.repository.CategoryRepository;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class CategoryService {

    CategoryRepository categoryRepository;
    CategoryMapper categoryMapper;

    public CategoryResponse createCategory(CategoryRequest request) {
        if (categoryRepository.findByCategoryCode(request.getCategoryCode()).isPresent()) {
            throw new AppException(ErrorCode.CATEGORY_CODE_EXIST);
        }

        Category category = categoryMapper.toEntity(request);
        category.setCategoryId(java.util.UUID.randomUUID().toString());

        return categoryMapper.toResponse(categoryRepository.save(category));
    }

    public CategoryResponse getCategoryByCode(String categoryCode) {
        Category category = categoryRepository.findByCategoryCode(categoryCode)
                .orElseThrow(() -> new AppException(ErrorCode.CATEGORY_NOT_FOUND));

        return categoryMapper.toResponse(category);
    }

    public List<CategoryResponse> getAllCategories() {
        return categoryRepository.findAll().stream()
                .map(categoryMapper::toResponse)
                .collect(Collectors.toList());
    }

    public CategoryResponse updateCategory(String categoryCode, CategoryRequest request) {
        Category category = categoryRepository.findByCategoryCode(categoryCode)
                .orElseThrow(() -> new AppException(ErrorCode.CATEGORY_NOT_FOUND));

        category.setCategoryName(request.getCategoryName());
        return categoryMapper.toResponse(categoryRepository.save(category));
    }

    public void deleteCategory(String categoryCode) {
        Category category = categoryRepository.findByCategoryCode(categoryCode)
                .orElseThrow(() -> new AppException(ErrorCode.CATEGORY_NOT_FOUND));

        categoryRepository.delete(category);
    }
}
