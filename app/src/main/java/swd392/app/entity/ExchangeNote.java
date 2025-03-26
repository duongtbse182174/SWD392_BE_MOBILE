package swd392.app.entity;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;
import swd392.app.enums.StockExchangeStatus;
import swd392.app.enums.StockTransactionType;

import java.time.LocalDate;
import java.time.LocalDateTime;
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
    LocalDateTime date;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, columnDefinition = "ENUM('pending', 'accepted', 'finished', 'rejected') DEFAULT 'pending'")
    StockExchangeStatus status;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, columnDefinition = "ENUM('IMPORT', 'EXPORT')")
    StockTransactionType transactionType;

    @ManyToOne
    @JoinColumn(name = "source_warehouse_code", referencedColumnName = "warehouse_code", nullable = true)
    Warehouse sourceWarehouse;

    @ManyToOne
    @JoinColumn(name = "destination_warehouse_code", referencedColumnName = "warehouse_code", nullable = true)
    Warehouse destinationWarehouse;

    @ManyToOne
    @JoinColumn(name = "created_by", referencedColumnName = "user_code", nullable = false)
    User createdBy;

    @ManyToOne
    @JoinColumn(name = "approved_by", referencedColumnName = "user_code")
    User approvedBy;

    @OneToMany(mappedBy = "exchangeNote", cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    List<NoteItem> noteItems;

    @Transient // Trường tạm thời không lưu vào database
    private List<NoteItem> transientNoteItems;
}
