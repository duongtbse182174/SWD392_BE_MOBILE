package swd392.app.dto.request;

import lombok.*;
import lombok.experimental.FieldDefaults;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ProductTypeRequest {
    String productTypeCode;
    String productTypeName;
    Double price;
    String categoryCode;  // Mối quan hệ với Category
}
