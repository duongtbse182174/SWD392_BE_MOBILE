package swd392.app.dto.response;

import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class StockResponse {
    String stockCode;
    String productCode;
    String productName;
    Integer quantity;
}