package it.uniroma3.siw.R3cap.repository;

import it.uniroma3.siw.R3cap.model.Note;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface NoteRepository extends JpaRepository<Note, Long> {
    List<Note> findByTitleContainingIgnoreCaseAndUploader_CorsoDiStudi(String title, String corsoDiStudi);
}
