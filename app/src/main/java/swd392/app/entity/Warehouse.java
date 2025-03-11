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
@Table(name = "Warehouse")
public class Warehouse {
    @Id
    @Column(name = "warehouse_id")
    String warehouseId;

    @Column(name = "warehouse_code", nullable = false, unique = true, length = 6)
    String warehouseCode;

    @Column(name = "warehouse_name", nullable = false)
    String warehouseName;

    @Column(name = "address", nullable = false)
    String address;
}