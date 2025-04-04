package swd392.app.entity;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;
import swd392.app.enums.NoteItemStatus;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@Entity
@Table(name = "NoteItem")
public class NoteItem {
    @Id
    @Column(name = "noteItem_id")
    String noteItemId;

    @Column(name = "noteItem_code", nullable = false, unique = true, length = 6)
    String noteItemCode;

    @ManyToOne
    @JoinColumn(name = "product_code", referencedColumnName = "product_code", nullable = false)
    Product product;

    @ManyToOne
    @JoinColumn(name = "exchangeNote_id", nullable = false)
    ExchangeNote exchangeNote;

    @Column(name = "quantity", nullable = false)
    int quantity;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    NoteItemStatus status;
}