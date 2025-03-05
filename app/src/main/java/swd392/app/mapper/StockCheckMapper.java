//package swd392.app.mapper;
//
//import org.mapstruct.Mapper;
//import org.mapstruct.Mapping;
//import org.mapstruct.Named;
//import swd392.app.dto.request.StockCheckNoteRequest;
//import swd392.app.dto.request.StockCheckProductRequest;
//import swd392.app.dto.response.StockCheckProductResponse;
//import swd392.app.dto.response.StockCheckNoteResponse;
//import swd392.app.entity.StockCheckNote;
//import swd392.app.entity.StockCheckProduct;
//
//import java.util.List;
//import java.util.stream.Collectors;
//
//@Mapper(componentModel = "spring")
//public interface StockCheckMapper {
//
//    //Chuyển đổi StockCheckNote entity thành StockCheckResponse
//    @Mapping(source = "stockCheckNoteId", target = "stockCheckNoteId")
//    @Mapping(source = "date", target = "date")
//    @Mapping(source = "warehouse.warehouseCode", target = "warehouseCode")
//    @Mapping(source = "warehouse.warehouseName", target = "warehouseName")
//    @Mapping(source = "checker.fullName", target = "checkerName")
//    StockCheckNoteResponse toStockCheckNoteResponse(StockCheckNote stockCheckNote);
//
//
//    //Chuyển đổi StockCheckProduct entity thành StockCheckProductResponse
//    @Mapping(source = "product.productCode", target = "productCode")
//    @Mapping(source = "product.productName", target = "productName")
//    @Mapping(source = "expectedQuantity", target = "expectedQuantity")
//    @Mapping(source = "actualQuantity", target = "actualQuantity")
//    @Mapping(expression = "java(stockCheckProduct.getActualQuantity() - stockCheckProduct.getExpectedQuantity())", target = "difference")
//    StockCheckProductResponse toStockCheckProductResponse(StockCheckProduct stockCheckProduct);
//
//    // Chuyển đổi List<StockCheckProduct> thành List<StockCheckProductResponse>
//    default List<StockCheckProductResponse> toStockCheckProductResponses(List<StockCheckProduct> stockCheckProducts) {
//        return stockCheckProducts.stream()
//                .map(this::toStockCheckProductResponse)
//                .collect(Collectors.toList());
//    }
//}
//
//

package swd392.app.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Mappings;
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
            @Mapping(source = "date", target = "date"),
            @Mapping(source = "warehouse.warehouseCode", target = "warehouseCode"),
            @Mapping(source = "warehouse.warehouseName", target = "warehouseName"),
            @Mapping(source = "checker.fullName", target = "checkerName"),
            @Mapping(source = "stockCheckProducts", target = "stockCheckProducts")
    })
    StockCheckNoteResponse toStockCheckNoteResponse(StockCheckNote stockCheckNote);

    // Chuyển đổi StockCheckProduct entity thành StockCheckProductResponse
    @Mappings({
            @Mapping(source = "product.productCode", target = "productCode"),
            @Mapping(source = "product.productName", target = "productName"),
            @Mapping(source = "expectedQuantity", target = "expectedQuantity"),
            @Mapping(source = "actualQuantity", target = "actualQuantity"),
            @Mapping(expression = "java(stockCheckProduct.getActualQuantity() - stockCheckProduct.getExpectedQuantity())", target = "difference")
    })
    StockCheckProductResponse toStockCheckProductResponse(StockCheckProduct stockCheckProduct);

    // Chuyển đổi List<StockCheckProduct> thành List<StockCheckProductResponse>
    default List<StockCheckProductResponse> toStockCheckProductResponses(List<StockCheckProduct> stockCheckProducts) {
        return stockCheckProducts.stream()
                .map(this::toStockCheckProductResponse)
                .collect(Collectors.toList());
    }
}
