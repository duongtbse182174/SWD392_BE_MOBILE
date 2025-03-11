package swd392.app.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import swd392.app.dto.response.NoteItemResponse;
import swd392.app.dto.response.StockExchangeResponse;
import swd392.app.entity.ExchangeNote;
import swd392.app.entity.NoteItem;

import java.util.List;
import java.util.stream.Collectors;

@Mapper(componentModel = "spring")
public interface StockTransactionMapper {
    @Mapping(source = "exchangeNoteId", target = "transactionId")
    @Mapping(source = "transactionType", target = "transactionType")
    @Mapping(source = "sourceWarehouse.warehouseId", target = "sourceWarehouseId")
    @Mapping(source = "destinationWarehouse.warehouseId", target = "destinationWarehouseId")
    @Mapping(source = "createdBy.userCode", target = "createdBy")
    @Mapping(source = "approvedBy.userCode", target = "approvedBy")
    @Mapping(source = "status", target = "status")
    @Mapping(source = "noteItems", target = "items", qualifiedByName = "mapNoteItems")
    StockExchangeResponse toResponse(ExchangeNote transaction);

    @Named("mapNoteItems")
    default List<NoteItemResponse> mapNoteItems(List<NoteItem> noteItems) {
        if (noteItems == null) return List.of();

        return noteItems.stream().map(item -> NoteItemResponse.builder()
                .noteItemCode(item.getNoteItemCode())
                .productCode(item.getProduct().getProductCode())
                .quantity(item.getQuantity())
                .build()).collect(Collectors.toList());
    }
}