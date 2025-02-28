package swd392.app.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import swd392.app.dto.response.StockResponse;
import swd392.app.entity.Stock;

@Mapper(componentModel = "spring")
public interface StockMapper {
//    @Mapping(source = "stockId", target = "id")  // Đảm bảo ID được map chính xác
    @Mapping(source = "stockCode", target = "stockCode")
    @Mapping(source = "product.productCode", target = "productCode")
    @Mapping(source = "product.productName", target = "productName")
    @Mapping(source = "quantity", target = "quantity")
    StockResponse toStockResponse(Stock stock);
}