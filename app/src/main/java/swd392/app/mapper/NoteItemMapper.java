package swd392.app.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import swd392.app.dto.response.NoteItemResponse;
import swd392.app.entity.NoteItem;

@Mapper(componentModel = "spring")
public interface NoteItemMapper {
    @Mapping(source = "noteItemCode", target = "noteItemCode")
    @Mapping(source = "product.productCode", target = "productCode")
    @Mapping(source = "quantity", target = "quantity")
    NoteItemResponse toResponse(NoteItem noteItem);
}
