package swd392.app.entity;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;
import swd392.app.enums.StockExchangeStatus;
import swd392.app.enums.StockTransactionType;

import java.time.LocalDate;
import java.util.List;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@Entity
@Table(name = "ExchangeNote")
public class ExchangeNote {
    @Id
    @Column(name = "exchangeNote_id")
    String exchangeNoteId;

    @Column(nullable = false)
    LocalDate date;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, columnDefinition = "ENUM('pending', 'accepted', 'finished', 'rejected') DEFAULT 'pending'")
    StockExchangeStatus status;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, columnDefinition = "ENUM('IMPORT', 'EXPORT', 'TRANSFER')")
    StockTransactionType transactionType;

    @Column(name = "source_type", nullable = false, columnDefinition = "ENUM('EXTERNAL', 'INTERNAL', 'SYSTEM') DEFAULT 'EXTERNAL'")
    String sourceType;

    @ManyToOne
    @JoinColumn(name = "source_warehouse_id", referencedColumnName = "warehouse_code", nullable = false)
    Warehouse sourceWarehouse;

    @ManyToOne
    @JoinColumn(name = "destination_warehouse_id", referencedColumnName = "warehouse_code")
    Warehouse destinationWarehouse;

    @ManyToOne
    @JoinColumn(name = "created_by", referencedColumnName = "user_code", nullable = false)
    User createdBy;

    @ManyToOne
    @JoinColumn(name = "approved_by", referencedColumnName = "user_code", insertable = false, updatable = false)
    User approvedBy;

    @OneToMany(mappedBy = "exchangeNote", cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    List<NoteItem> noteItems;
}
