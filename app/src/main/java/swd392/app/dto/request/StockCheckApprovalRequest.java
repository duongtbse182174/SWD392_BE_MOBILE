package swd392.app.dto.request;

import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class StockCheckApprovalRequest {
    String stockCheckNoteId;
    public Boolean isApproved;
}

