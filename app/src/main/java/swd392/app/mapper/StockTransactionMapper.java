package swd392.app.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import swd392.app.dto.response.StockExchangeResponse;
import swd392.app.entity.ExchangeNote;

@Mapper(componentModel = "spring")
public interface StockTransactionMapper {
    @Mapping(source = "exchangeNoteId", target = "transactionId")
    @Mapping(source = "transactionType", target = "transactionType")
    @Mapping(source = "sourceWarehouse.warehouseId", target = "sourceWarehouseId")
    @Mapping(source = "destinationWarehouse.warehouseId", target = "destinationWarehouseId")
    @Mapping(source = "createdBy.userCode", target = "createdBy")
//    @Mapping(source = "approvedBy.userCode", target = "approvedBy", defaultValue = "")
    @Mapping(source = "status", target = "status")
    StockExchangeResponse toResponse(ExchangeNote transaction);
}
