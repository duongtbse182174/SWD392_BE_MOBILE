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
@Table(name = "Category")
public class Category {
    @Id
    String categoryId;

    @Column(nullable = false, unique = true, length = 6)
    String categoryCode;

    @Column(nullable = false)
    String categoryName;
}
