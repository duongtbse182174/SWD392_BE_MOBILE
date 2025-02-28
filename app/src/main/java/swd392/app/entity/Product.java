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
@Table(name = "Product")
public class Product {
    @Id
    String productId;

    @Column(name = "product_code", nullable = false, unique = true, length = 6)
    String productCode;

    @Column(nullable = false)
    String productName;

    @Column(nullable = false)
    String size;

    @Column(nullable = false)
    String color;

    @Column(nullable = false)
    Integer quantity;

    @ManyToOne
    @JoinColumn(name = "productType_code", referencedColumnName = "productType_code", nullable = false)
    ProductType productType;
}