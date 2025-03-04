package swd392.app.dto.response;

import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class NoteItemResponse {
    String noteItemCode;
    String productCode;
    int quantity;
}
