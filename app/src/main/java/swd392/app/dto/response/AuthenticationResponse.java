package swd392.app.dto.response;

import lombok.*;
import lombok.experimental.FieldDefaults;
import swd392.app.entity.Role;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class AuthenticationResponse {

    String token;
    boolean authenticated;

    String userCode;
    String userName;
    String fullName;
    String email;
    Role role;
}

