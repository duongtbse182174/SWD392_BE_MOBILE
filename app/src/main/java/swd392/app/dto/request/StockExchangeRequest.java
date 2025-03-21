package swd392.app.dto.request;

import lombok.*;
import lombok.experimental.FieldDefaults;
import swd392.app.enums.StockTransactionType;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class StockExchangeRequest {
    String transactionId;
    StockTransactionType transactionType;
    String sourceWarehouseCode;
    String destinationWarehouseCode;
    String createdBy;
    String approvedBy;
    List<TransactionItemRequest> items;
}
