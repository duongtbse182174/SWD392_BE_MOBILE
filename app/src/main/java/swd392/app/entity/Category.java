package swd392.app.entity;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@Entity
@Table(name = "Category")
public class Category {
    @Id
    @Column(name = "category_id")
    String categoryId;

    @Column(name = "category_code", nullable = false, unique = true, length = 50)
    String categoryCode;

    @Column(name = "category_name", nullable = false)
    String categoryName;
}