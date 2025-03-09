package swd392.app.dto.request;

import lombok.*;
import lombok.experimental.FieldDefaults;
import swd392.app.entity.NoteItem;
import swd392.app.entity.User;
import swd392.app.enums.SourceType;
import swd392.app.enums.StockTransactionType;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class StockExchangeRequest {
    String transactionId;
    StockTransactionType transactionType;
    SourceType sourceType;
    String sourceWarehouseId;
    String destinationWarehouseId;
    String createdBy;
    List<TransactionItemRequest> items;
}
