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

    @Column(name = "category_code", nullable = false, unique = true)
    private String categoryCode;

    @Column(nullable = false)
    String categoryName;
}
