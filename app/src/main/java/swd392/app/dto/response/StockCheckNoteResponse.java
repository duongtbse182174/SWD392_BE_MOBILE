package swd392.app.dto.response;

import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDate;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class StockCheckNoteResponse { //DTO cho phản hồi phiếu kiểm kho
    String stockCheckNoteId;
    LocalDate date;
    String warehouseCode;
    String warehouseName;
    String checkerName;
    String stockCheckStatus;
    List<StockCheckProductResponse> stockCheckProducts;
}
