package swd392.app.dto.request;

import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class StockCheckNoteRequest { //DTO cho yêu cầu tạo phiếu kiểm kho
    String warehouseCode;
    List<StockCheckProductRequest> stockCheckProducts;
}
