package swd392.app.dto.request;


import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class TransactionItemRequest {
    String noteItemCode;
    String productCode;
    Integer quantity;
}
