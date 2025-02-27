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
@Table(name = "Warehouse")
public class Warehouse {
    @Id
    String warehouseId;

    @Column(name = "warehouse_code", nullable = false, unique = true)
    private String warehouseCode;

    @Column(nullable = false)
    String warehouseName;

    @Column(nullable = false)
    String address;
}
