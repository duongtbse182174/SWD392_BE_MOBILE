package swd392.app.service;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import swd392.app.dto.request.ProductRequest;
import swd392.app.dto.response.ProductResponse;
import swd392.app.entity.Product;
import swd392.app.entity.ProductType;
import swd392.app.enums.ProductStatus;
import swd392.app.exception.AppException;
import swd392.app.exception.ErrorCode;
import swd392.app.mapper.ProductMapper;
import swd392.app.repository.ProductRepository;
import swd392.app.repository.ProductTypeRepository;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class ProductService {

    ProductRepository productRepository;
    ProductTypeRepository productTypeRepository;
    ProductMapper productMapper;

    public ProductResponse createProduct(ProductRequest request) {
        if (productRepository.findByProductCode(request.getProductCode()).isPresent()) {
            throw new AppException(ErrorCode.PRODUCT_CODE_EXIST);
        }

        ProductType productType = productTypeRepository.findByProductTypeCode(request.getProductTypeCode())
                .orElseThrow(() -> new AppException(ErrorCode.PRODUCT_TYPE_NOT_FOUND));

        Product product = productMapper.toEntity(request);
        product.setProductId(java.util.UUID.randomUUID().toString());
        product.setProductType(productType);
        product.setStatus(ProductStatus.instock);

        return productMapper.toResponse(productRepository.save(product));
    }

    public ProductResponse getProductByCode(String productCode) {
        Product product = productRepository.findByProductCode(productCode)
                .orElseThrow(() -> new AppException(ErrorCode.PRODUCT_NOT_FOUND));

        return productMapper.toResponse(product);
    }

    public List<ProductResponse> getAllProducts() {
        return productRepository.findAll().stream()
                .map(productMapper::toResponse)
                .collect(Collectors.toList());
    }

    public ProductResponse updateProduct(String productCode, ProductRequest request) {
        Product product = productRepository.findByProductCode(productCode)
                .orElseThrow(() -> new AppException(ErrorCode.PRODUCT_NOT_FOUND));

        product.setProductName(request.getProductName());
        product.setSize(request.getSize());
        product.setColor(request.getColor());
        product.setQuantity(request.getQuantity());

        return productMapper.toResponse(productRepository.save(product));
    }

    public void deleteProduct(String productCode) {
        Product product = productRepository.findByProductCode(productCode)
                .orElseThrow(() -> new AppException(ErrorCode.PRODUCT_NOT_FOUND));

        product.setStatus(ProductStatus.outofstock);
        productRepository.save(product); // Lưu lại với trạng thái đã thay đổi
    }
}
