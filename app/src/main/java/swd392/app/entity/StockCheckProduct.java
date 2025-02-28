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
@Table(name = "StockCheckProduct")
public class StockCheckProduct {
    @Id
    String stockCheckProductId;

    @ManyToOne
    @JoinColumn(name = "stockCheckNote_id", nullable = false)
    StockCheckNote stockCheckNote;

    @ManyToOne
    @JoinColumn(name = "product_code", referencedColumnName = "product_code", nullable = false)
    Product product;

    @Column(nullable = false)
    Integer expectedQuantity;

    @Column(nullable = false)
    Integer actualQuantity;

    @Column(insertable = false, updatable = false)
    Integer difference;
}
