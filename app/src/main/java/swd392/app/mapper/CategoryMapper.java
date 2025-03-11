package swd392.app.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import swd392.app.dto.request.CategoryRequest;
import swd392.app.dto.response.CategoryResponse;
import swd392.app.entity.Category;

@Mapper(componentModel = "spring")
public interface CategoryMapper {
    Category toEntity(CategoryRequest request);
    CategoryResponse toResponse(Category category);
}
