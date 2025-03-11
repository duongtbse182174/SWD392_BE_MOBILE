package swd392.app.dto.response;

import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class StockCheckProductResponse { //DTO cho phản hồi thông tin sản phẩm kiểm kho
    String productCode;
    String productName;
    Integer lastQuantity;
    Integer totalImportQuantity;
    Integer totalExportQuantity;
    Integer expectedQuantity;
    Integer actualQuantity;
    Integer difference;
}
