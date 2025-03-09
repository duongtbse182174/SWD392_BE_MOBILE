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
@Table(name = "StockCheckProduct")
public class StockCheckProduct {
    @Id
    @Column(name = "stockCheckProduct_id")
    String stockCheckProductId;

    @ManyToOne
    @JoinColumn(name = "stockCheckNote_id", nullable = false)
    StockCheckNote stockCheckNote;

    @ManyToOne
    @JoinColumn(name = "product_code", referencedColumnName = "product_code", nullable = false)
    Product product;

    @Column(name = "last_quantity", nullable = false)
    int lastQuantity;

    @Column(name = "actual_quantity", nullable = false)
    int actualQuantity;

    @Column(name = "difference", insertable = false, updatable = false)
    int difference;
}