package swd392.app.entity;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
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
    Integer lastQuantity;

    @Column(name = "actual_quantity", nullable = false)
    Integer actualQuantity;

    @Column(name = "total_import_quantity", nullable = false)
    Integer totalImportQuantity = 0;

    @Column(name = "total_export_quantity", nullable = false)
    Integer totalExportQuantity = 0;

    @Column(name = "expected_quantity", nullable = false)
    Integer expectedQuantity;

    public void calculateTheoreticalQuantity() {
        this.expectedQuantity = this.lastQuantity
                + this.totalImportQuantity
                - this.totalExportQuantity;
    }
}
