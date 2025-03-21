package swd392.app.dto.response;

import lombok.*;
import lombok.experimental.FieldDefaults;
import swd392.app.entity.Role;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class UserResponse {
    String id;
    String userCode;
    String userName;
    String fullName;
    String email;
    String warehouseCode;
    Role role;
}

