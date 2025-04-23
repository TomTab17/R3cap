package it.uniroma3.siw.R3cap.repository;

import it.uniroma3.siw.R3cap.model.Note;
import org.springframework.data.jpa.repository.JpaRepository;

public interface NoteRepository extends JpaRepository<Note, Long> {
}
