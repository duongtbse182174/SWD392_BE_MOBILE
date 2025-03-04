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
@Table(name = "NoteItem")
public class NoteItem {
    @Id
    @Column(name = "noteItem_id")
    String noteItemId;

    @Column(name = "noteItem_code",nullable = false, unique = true, length = 6)
    String noteItemCode;

    @ManyToOne
    @JoinColumn(name = "product_code", referencedColumnName = "product_code", nullable = false)
    Product product;

    @ManyToOne
    @JoinColumn(name = "exchangeNote_id", nullable = false)
    ExchangeNote exchangeNote;

    @Column(nullable = false)
    Integer quantity;
}
