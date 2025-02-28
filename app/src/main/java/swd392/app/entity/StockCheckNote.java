package swd392.app.entity;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;
import java.time.LocalDate;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@Entity
@Table(name = "StockCheckNote")
public class StockCheckNote {
    @Id
    String stockCheckNoteId;

    @Column(nullable = false)
    LocalDate date;

    @ManyToOne
    @JoinColumn(name = "warehouse_code", referencedColumnName = "warehouse_code", nullable = false)
    Warehouse warehouse;

    @ManyToOne
    @JoinColumn(name = "checker", referencedColumnName = "user_id", nullable = false)
    User checker;

    @OneToMany(mappedBy = "stockCheckNote", cascade = CascadeType.ALL)
    List<StockCheckProduct> stockCheckProducts;
}