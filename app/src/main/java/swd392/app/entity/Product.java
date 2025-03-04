package swd392.app.entity;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;
import swd392.app.enums.ProductStatus;
import swd392.app.enums.UserStatus;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@Entity
@Table(name = "Product")
public class Product {
    @Id
    @Column(name = "product_id")
    String productId;

    @Column(name = "product_code", nullable = false, unique = true, length = 6)
    String productCode;

    @Column(name = "product_name",nullable = false)
    String productName;

    @Column(name = "size",nullable = false)
    String size;

    @Column(nullable = false)
    String color;

    @Column(nullable = false)
    Integer quantity;

    @Enumerated(EnumType.STRING)
    @Column(columnDefinition = "ENUM('instock', 'outofstock') DEFAULT 'instock'")
    ProductStatus status;

    @ManyToOne
    @JoinColumn(name = "productType_code", referencedColumnName = "productType_code", nullable = false)
    ProductType productType;
}