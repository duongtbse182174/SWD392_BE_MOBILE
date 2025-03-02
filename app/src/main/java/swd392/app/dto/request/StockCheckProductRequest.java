package swd392.app.dto.request;

import jakarta.validation.constraints.Min;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class StockCheckProductRequest { // DTO cho thông tin sản phẩm kiểm kho
    String productCode;

    @Min(value = 0, message = "Actual quantity must be at least 0")
    Integer actualQuantity;
}
