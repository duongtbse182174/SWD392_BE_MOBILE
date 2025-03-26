package swd392.app.mapper;

import org.mapstruct.*;
import swd392.app.dto.response.StockCheckNoteResponse;
import swd392.app.dto.response.StockCheckProductResponse;
import swd392.app.entity.StockCheckNote;
import swd392.app.entity.StockCheckProduct;

import java.util.List;
import java.util.stream.Collectors;

@Mapper(componentModel = "spring")
public interface StockCheckMapper {

    // Chuyển đổi StockCheckNote entity thành StockCheckNoteResponse
    @Mappings({
            @Mapping(source = "stockCheckNoteId", target = "stockCheckNoteId"),
            @Mapping(source = "dateTime", target = "dateTime"),
            @Mapping(source = "warehouse.warehouseCode", target = "warehouseCode"),
            @Mapping(source = "warehouse.warehouseName", target = "warehouseName"),
            @Mapping(source = "checker.fullName", target = "checkerName"),
            @Mapping(source = "stockCheckStatus", target = "stockCheckStatus"),
            @Mapping(source = "stockCheckProducts", target = "stockCheckProducts")
    })
    StockCheckNoteResponse toStockCheckNoteResponse(StockCheckNote stockCheckNote);

    @Mapping(source = "product.productCode", target = "productCode")
    @Mapping(source = "product.productName", target = "productName")
    @Mapping(source = "lastQuantity", target = "lastQuantity")
    @Mapping(source = "totalImportQuantity", target = "totalImportQuantity")
    @Mapping(source = "totalExportQuantity", target = "totalExportQuantity")
    @Mapping(source = "actualQuantity", target = "actualQuantity")
    @Mapping(expression = "java(stockCheckProduct.getActualQuantity() - stockCheckProduct.getExpectedQuantity())", target = "difference")
    StockCheckProductResponse toStockCheckProductResponse(StockCheckProduct stockCheckProduct);

   // Chuyển đổi List<StockCheckProduct> thành List<StockCheckProductResponse>
    default List<StockCheckProductResponse> toStockCheckProductResponses(List<StockCheckProduct> stockCheckProducts) {
        return stockCheckProducts.stream()
                .map(this::toStockCheckProductResponse)
                .collect(Collectors.toList());
    }
}