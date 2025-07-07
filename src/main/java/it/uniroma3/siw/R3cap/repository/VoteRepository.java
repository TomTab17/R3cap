package it.uniroma3.siw.R3cap.repository;

import it.uniroma3.siw.R3cap.model.Note;
import it.uniroma3.siw.R3cap.model.User;
import it.uniroma3.siw.R3cap.model.Vote;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface VoteRepository extends JpaRepository<Vote, Long> {
    Optional<Vote> findByVoterAndNote(User voter, Note note);
    List<Vote> findByNote(Note note);
    List<Vote> findByNote_Uploader(User uploader);
    List<Vote> findByNote_UploaderAndNote_Uploader_CorsoDiStudi(User uploader, String corsoDiStudi);
    int countByNoteAndValue(Note note, int value);
}