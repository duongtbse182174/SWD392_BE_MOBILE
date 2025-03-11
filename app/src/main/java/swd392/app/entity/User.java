package swd392.app.entity;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;
import swd392.app.enums.UserStatus;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@Entity
@Table(name = "User")
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "user_id")
    String userId;

    @Column(name = "user_code",nullable = false, unique = true, length = 6)
    String userCode;

    @ManyToOne
    @JoinColumn(name = "role_id", nullable = false)
    Role role;

    @Column(name = "user_name",nullable = false)
    String userName;

    @Column(name = "full_name",nullable = false)
    String fullName;

    @Column(name = "email",nullable = false)
    String email;

    @Column(name = "password",nullable = false)
    String password;

    @ManyToOne
    @JoinColumn(name = "warehouse_code", referencedColumnName = "warehouse_code")
    Warehouse warehouse;

    @Enumerated(EnumType.STRING)
    @Column(columnDefinition = "ENUM('active', 'inactive') DEFAULT 'inactive'")
    UserStatus status;

    @Column(name = "created_at",updatable = false)
    LocalDateTime createdAt;
    @Column(name = "updated_at",updatable = false)
    LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

}