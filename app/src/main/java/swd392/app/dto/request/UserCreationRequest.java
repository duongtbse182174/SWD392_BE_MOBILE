//package swd392.app.dto.request;
//
//import jakarta.persistence.PrePersist;
//import jakarta.validation.constraints.Pattern;
//import jakarta.validation.constraints.Size;
//import lombok.*;
//import lombok.experimental.FieldDefaults;
//
//@Data
//@Builder
//@NoArgsConstructor
//@AllArgsConstructor
//@FieldDefaults(level = AccessLevel.PRIVATE)
//public class UserCreationRequest {
//
//    @Size(min = 3, message = "USERNAME_INVALID")
//    String name;
//
//    @Size(min = 8, message = "")
//    String fullName;
//
//    @Pattern(
//            regexp = "^[A-Za-z0-9_]+@gmail.com",
//            message = "EMAIL_INVALID"
//    )
//    String email;
//
//    @Size(min = 8, message = "PASSWORD_INVALID")
//    String password;
//
//    private Boolean active;
//    @PrePersist
//    protected void onCreate() {
//        if (active == null) {
//            active = true;
//        }
//    }
//}
//

package swd392.app.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.*;
import lombok.experimental.FieldDefaults;
import swd392.app.enums.UserStatus;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class UserCreationRequest {

//    @NotBlank(message = "USER_CODE_REQUIRED")
    @Size(min = 6, max = 6, message = "USER_CODE_INVALID")
    String userCode;

    @NotNull(message = "ROLE_REQUIRED")
    Long roleId; // Vì role là @ManyToOne, ta chỉ cần truyền ID

//    @NotBlank(message = "USERNAME_REQUIRED")
    @Size(min = 3, message = "USERNAME_INVALID")
    String userName;

    @NotBlank(message = "FULLNAME_REQUIRED")
    String fullName;

//    @NotBlank(message = "EMAIL_REQUIRED")
    @Email(message = "EMAIL_INVALID")
    String email;

//    @NotBlank(message = "PASSWORD_REQUIRED")
    @Size(min = 8, message = "PASSWORD_INVALID")
    String password;

    Long warehouseId; // ID của warehouse (nếu có)

//    @NotNull(message = "STATUS_REQUIRED")
    UserStatus status;
}

