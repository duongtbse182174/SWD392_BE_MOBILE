package swd392.app.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import swd392.app.dto.request.ProductRequest;
import swd392.app.dto.response.ProductResponse;
import swd392.app.entity.Product;

@Mapper(componentModel = "spring")
public interface ProductMapper {
    @Mapping(target = "productType", ignore = true)
    Product toEntity(ProductRequest request);

    @Mapping(source = "productType.productTypeName", target = "productTypeName")
    ProductResponse toResponse(Product product);
}
