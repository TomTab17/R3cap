package it.uniroma3.siw.R3cap.controller;

import it.uniroma3.siw.R3cap.model.Note;
import it.uniroma3.siw.R3cap.model.User;
import it.uniroma3.siw.R3cap.repository.NoteRepository;
import it.uniroma3.siw.R3cap.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.*;
import java.security.Principal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Controller
@RequestMapping("/notes")
public class NoteController {

    @Autowired
    private NoteRepository noteRepository;

    @Autowired
    private UserRepository userRepository;

    // Metodo per ottenere l'utente autenticato
    private Optional<User> getAuthenticatedUser(Principal principal) {
        if (principal == null) {
            return Optional.empty();
        }
        return userRepository.findByUsername(principal.getName());
    }

    // Mostra il form di upload
    @GetMapping("/upload")
    public String showUploadForm(Principal principal) {
        if (principal == null) {
            return "redirect:/login";  // Reindirizza se l'utente non è autenticato
        }
        return "upload";
    }

    // Gestisce l'upload dei file
    @PostMapping("/upload")
    public String uploadNote(
            @RequestParam("title") String title,
            @RequestParam("description") String description,
            @RequestParam("file") MultipartFile file,
            Principal principal
    ) throws IOException {

        // Controllo se il file è vuoto
        if (file.isEmpty()) {
            return "redirect:/upload?error=fileEmpty";
        }

        // Recupera l'utente autenticato
        Optional<User> optionalUser = getAuthenticatedUser(principal);
        if (optionalUser.isEmpty()) return "redirect:/login";

        // Imposta la directory di upload
        String uploadDir = "uploads/";
        String newFileName = UUID.randomUUID().toString() + "_" + file.getOriginalFilename();
        Path path = Paths.get(uploadDir, newFileName);

        // Crea la directory se non esiste
        Files.createDirectories(path.getParent());

        // Copia il file nella destinazione
        Files.copy(file.getInputStream(), path, StandardCopyOption.REPLACE_EXISTING);

        // Crea una nuova nota
        Note note = new Note();
        note.setTitle(title);
        note.setDescription(description);
        note.setFilePath(path.toString());
        note.setUploadDate(LocalDateTime.now());
        note.setUploader(optionalUser.get());

        // Salva la nota nel database
        noteRepository.save(note);

        return "redirect:/";  // Reindirizza alla home
    }

    // Gestisce la ricerca delle note
    @GetMapping("/search")
    public String searchNotes(@RequestParam("query") String query, Model model, Principal principal) {
        Optional<User> optionalUser = getAuthenticatedUser(principal);
        if (optionalUser.isEmpty()) return "redirect:/login";

        String corso = optionalUser.get().getCorsoDiStudi();
        List<Note> results = noteRepository.findByTitleContainingIgnoreCaseAndUploader_CorsoDiStudi(query, corso);

        model.addAttribute("results", results);
        model.addAttribute("query", query);
        return "searchResults";
    }
}
