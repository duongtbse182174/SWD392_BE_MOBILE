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
@Table(name = "ExchangeNote")
public class ExchangeNote {
    @Id
    String exchangeNoteId;

    @Column(nullable = false)
    LocalDate date;

    @OneToMany(mappedBy = "exchangeNote", cascade = CascadeType.ALL)
    List<NoteItem> noteItems;
}