package swd392.app.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import swd392.app.entity.NoteItem;

@Repository
public interface NoteItemRepository extends JpaRepository<NoteItem, String> {
}
