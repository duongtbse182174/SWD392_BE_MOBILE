package swd392.app.entity;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@Entity
@Table(name = "Role")
public class Role {
    @Id
    String roleId;

//    @Enumerated(EnumType.STRING)
    @Column(nullable = false, unique = true)
    String roleType;

    @Column(nullable = false)
    String roleName;

//    public enum RoleType {
//        ADMIN,
//        MANAGER,
//        STAFF
//    }
}
