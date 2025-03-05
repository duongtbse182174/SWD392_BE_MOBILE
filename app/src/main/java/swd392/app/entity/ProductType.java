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
@Table(name = "ProductType")
public class ProductType {
    @Id
    @Column(name = "productType_id")
    String productTypeId;

    @Column(name = "productType_code", nullable = false, unique = true, length = 50)
    String productTypeCode;

    @Column(name = "productType_name",nullable = false)
    String productTypeName;

    @Column(nullable = true)
    Double price;

    @ManyToOne
    @JoinColumn(name = "category_code", referencedColumnName = "category_code", nullable = false)
    Category category;
}
