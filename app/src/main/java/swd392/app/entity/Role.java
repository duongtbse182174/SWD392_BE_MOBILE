package swd392.app.entity;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@Entity
@Table(name = "Role")
public class Role {
    @Id
    @Column(name = "role_id")
    String roleId;

    @Column(name = "role_type")
    String roleType;

    @Column(name = "role_name", nullable = false)
    String roleName;
}