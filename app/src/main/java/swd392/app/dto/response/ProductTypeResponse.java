package swd392.app.dto.response;

import lombok.*;
import lombok.experimental.FieldDefaults;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ProductTypeResponse {
    String productTypeCode;
    String productTypeName;
    Double price;
    String categoryName;  // Hiển thị thông tin Category đi kèm
}
