package swd392.app.dto.response;

import lombok.*;
import lombok.experimental.FieldDefaults;
import swd392.app.enums.StockExchangeStatus;
import swd392.app.enums.StockTransactionType;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class StockExchangeResponse {
    String transactionId;
    StockTransactionType transactionType;
    String sourceWarehouseId;
    String destinationWarehouseId;
    String createdBy;
    String approvedBy;
    StockExchangeStatus status;
    List<NoteItemResponse> items;
}
