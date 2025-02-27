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
    String productTypeId;

    @Column(name = "productType_code", nullable = false, unique = true, length = 6)
    String productTypeCode;

    @Column(nullable = false)
    String productTypeName;

    @Column(nullable = false)
    Double price;

    @ManyToOne
    @JoinColumn(name = "category_code", referencedColumnName = "category_code", nullable = false)
    Category category;
}
