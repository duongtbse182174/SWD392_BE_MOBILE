//package swd392.app.dto.request;
//
//import lombok.*;
//import lombok.experimental.FieldDefaults;
//
//@Data
//@Builder
//@NoArgsConstructor
//@AllArgsConstructor
//@FieldDefaults(level = AccessLevel.PRIVATE)
//public class UserUpdateRequest {
//    private String name;
//    private String fullName;
//    private String email;
//    private String password;
//}
//
package swd392.app.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class UserUpdateRequest {

//    @Size(min = 3, max = 6, message = "USER_CODE_INVALID")
    String userCode;

    @Size(min = 3, message = "USERNAME_INVALID")
    String userName;

    String fullName;

    @Email(message = "EMAIL_INVALID")
    String email;

    @Size(min = 8, message = "PASSWORD_INVALID")
    String password;

    String roleId;

    String warehouseCode;
}

