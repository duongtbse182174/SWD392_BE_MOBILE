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
@Table(name = "Stock")
public class Stock {
    @Id
    @Column(name = "stock_id")
    String stockId;

    @Column(name = "stock_code",nullable = false, unique = true, length = 50)
    String stockCode;

    @ManyToOne
    @JoinColumn(name = "warehouse_code", referencedColumnName = "warehouse_code", nullable = false)
    Warehouse warehouse;

    @ManyToOne
    @JoinColumn(name = "product_code", referencedColumnName = "product_code", nullable = false)
    Product product;

    @Column(nullable = false)
    Integer quantity;
}