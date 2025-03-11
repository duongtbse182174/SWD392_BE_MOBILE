package swd392.app.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import swd392.app.dto.request.ProductTypeRequest;
import swd392.app.dto.response.ProductTypeResponse;
import swd392.app.entity.ProductType;

@Mapper(componentModel = "spring")
public interface ProductTypeMapper {
    @Mapping(target = "category", ignore = true)
    ProductType toEntity(ProductTypeRequest request);

    @Mapping(source = "category.categoryName", target = "categoryName")
    ProductTypeResponse toResponse(ProductType productType);
}
