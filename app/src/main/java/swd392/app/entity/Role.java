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
    @Column(name = "role_id")
    String role_id;

    @Column(name = "role_type", nullable = false, unique = true)
    String roleType;

    @Column(name = "role_name",nullable = false)
    String roleName;

}
