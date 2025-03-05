package swd392.app.entity;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;
import swd392.app.enums.StockCheckStatus;
import swd392.app.enums.UserStatus;

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
    @Column(name = "stockCheckNote_id")
    String stockCheckNoteId;

    @Column(nullable = false)
    LocalDate date;

    @ManyToOne
    @JoinColumn(name = "warehouse_code", referencedColumnName = "warehouse_code", nullable = false)
    Warehouse warehouse;

    @ManyToOne
    @JoinColumn(name = "checker", referencedColumnName = "user_code", nullable = false)
    User checker;

    @Column(name = "description", nullable = true)
    String description;

    @Enumerated(EnumType.STRING)
    @Column(name = "stockCheck_status", columnDefinition = "ENUM('pending', 'accepted', 'finished', 'rejected') DEFAULT 'pending'")
    StockCheckStatus stockCheckStatus;

    @OneToMany(mappedBy = "stockCheckNote", cascade = CascadeType.ALL)
    List<StockCheckProduct> stockCheckProducts;
}