package swd392.app.service;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import swd392.app.dto.request.ProductTypeRequest;
import swd392.app.dto.response.ProductTypeResponse;
import swd392.app.entity.Category;
import swd392.app.entity.ProductType;
import swd392.app.exception.AppException;
import swd392.app.exception.ErrorCode;
import swd392.app.mapper.ProductTypeMapper;
import swd392.app.repository.CategoryRepository;
import swd392.app.repository.ProductTypeRepository;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class ProductTypeService {

    ProductTypeRepository productTypeRepository;
    CategoryRepository categoryRepository;
    ProductTypeMapper productTypeMapper;

    public ProductTypeResponse createProductType(ProductTypeRequest request) {
        if (productTypeRepository.findByProductTypeCode(request.getProductTypeCode()).isPresent()) {
            throw new AppException(ErrorCode.PRODUCT_TYPE_CODE_EXIST);
        }

        Category category = categoryRepository.findByCategoryCode(request.getCategoryCode())
                .orElseThrow(() -> new AppException(ErrorCode.CATEGORY_NOT_FOUND));

        ProductType productType = productTypeMapper.toEntity(request);
        productType.setProductTypeId(java.util.UUID.randomUUID().toString());
        productType.setCategory(category);

        return productTypeMapper.toResponse(productTypeRepository.save(productType));
    }

    public ProductTypeResponse getProductTypeByCode(String productTypeCode) {
        ProductType productType = productTypeRepository.findByProductTypeCode(productTypeCode)
                .orElseThrow(() -> new AppException(ErrorCode.PRODUCT_TYPE_NOT_FOUND));

        return productTypeMapper.toResponse(productType);
    }

    public List<ProductTypeResponse> getAllProductTypes() {
        return productTypeRepository.findAll().stream()
                .map(productTypeMapper::toResponse)
                .collect(Collectors.toList());
    }

    public ProductTypeResponse updateProductType(String productTypeCode, ProductTypeRequest request) {
        ProductType productType = productTypeRepository.findByProductTypeCode(productTypeCode)
                .orElseThrow(() -> new AppException(ErrorCode.PRODUCT_TYPE_NOT_FOUND));

        productType.setProductTypeName(request.getProductTypeName());
        productType.setPrice(request.getPrice());

        return productTypeMapper.toResponse(productTypeRepository.save(productType));
    }

    public void deleteProductType(String productTypeCode) {
        ProductType productType = productTypeRepository.findByProductTypeCode(productTypeCode)
                .orElseThrow(() -> new AppException(ErrorCode.PRODUCT_TYPE_NOT_FOUND));

        productTypeRepository.delete(productType);
    }
}
