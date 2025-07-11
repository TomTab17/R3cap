package it.uniroma3.siw.R3cap.repository;

import it.uniroma3.siw.R3cap.model.Note;
import it.uniroma3.siw.R3cap.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface NoteRepository extends JpaRepository<Note, Long> {
    // Ricerco per titolo e corso di studi
    List<Note> findByTitleContainingIgnoreCaseAndUploader_CorsoDiStudi(String title, String corsoDiStudi);

    // Ricerco per titolo e corso di studi, ignorando il case del corso
    List<Note> findByTitleContainingIgnoreCaseAndUploader_CorsoDiStudiIgnoreCase(String title, String corsoDiStudi);

    // Ricerco solo per titolo (senza filtro per corso di studi)
    List<Note> findByTitleContainingIgnoreCase(String title);

    // Ricerco solo per utente
    List<Note> findByUploader(User uploader);
}
