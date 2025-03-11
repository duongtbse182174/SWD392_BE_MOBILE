package swd392.app.dto.request;

import lombok.*;
import lombok.experimental.FieldDefaults;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ProductRequest {
    String productCode;
    String productName;
    String size;
    String color;
    int quantity;
    String productTypeCode;  // Mối quan hệ với ProductType
}
