package swd392.app.entity;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;
import swd392.app.enums.StockCheckStatus;

import java.time.LocalDate;
import java.util.List;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@Entity
@Table(name = "StockCheckNote")
public class StockCheckNote {
    @Id
    @Column(name = "stockCheckNote_id")
    String stockCheckNoteId;

    @Column(name = "date", nullable = false)
    LocalDate date;

    @ManyToOne
    @JoinColumn(name = "warehouse_code", referencedColumnName = "warehouse_code", nullable = false)
    Warehouse warehouse;

    @ManyToOne
    @JoinColumn(name = "checker", referencedColumnName = "user_code", nullable = false)
    User checker;

    @Column(name = "description")
    String description;

    @Enumerated(EnumType.STRING)
    @Column(name = "stockCheck_status", columnDefinition = "ENUM('pending','approved', 'finished', 'rejected') DEFAULT 'pending'")
    StockCheckStatus stockCheckStatus;

    @OneToMany(mappedBy = "stockCheckNote", cascade = CascadeType.ALL)
    List<StockCheckProduct> stockCheckProducts;
}