package swd392.app.entity;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;
import swd392.app.enums.StockExchangeStatus;
import swd392.app.enums.SourceType;
import swd392.app.enums.StockTransactionType;

import java.time.LocalDateTime;

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

    @Column(name = "warehouse_code", nullable = false, length = 6)
    String warehouseCode;

    @Enumerated(EnumType.STRING)
    @Column(name = "transactionType", nullable = false)
    StockTransactionType transactionType;

    @Enumerated(EnumType.STRING)
    @Column(name = "source_type", nullable = false)
    SourceType sourceType;

    @Enumerated(EnumType.STRING)
    @Column(columnDefinition = "ENUM('pending', 'accepted', 'finished', 'rejected') DEFAULT 'pending'")
    StockExchangeStatus status;

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
    @JoinColumn(name = "approved_by", referencedColumnName = "user_code")
    User approvedBy;

    @Column(name = "date")
    LocalDateTime date;
}