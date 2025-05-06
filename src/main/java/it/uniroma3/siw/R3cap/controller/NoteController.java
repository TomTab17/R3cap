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

@Controller
@RequestMapping("/notes")
public class NoteController {

    @Autowired
    private NoteRepository noteRepository;

    @Autowired
    private UserRepository userRepository;

    @GetMapping("/upload")
    public String showUploadForm(Principal principal) {
        if (principal == null) {
            return "redirect:/login";  // Reindirizza se l'utente non è autenticato
        }
    return "upload";
    }

    @PostMapping("/upload")
    public String uploadNote(
            @RequestParam("title") String title,
            @RequestParam("description") String description,
            @RequestParam("file") MultipartFile file,
            Principal principal
    ) throws IOException {

        if (file.isEmpty()) return "redirect:/?error=file";

        Optional<User> optionalUser = userRepository.findByUsername(principal.getName());
        if (optionalUser.isEmpty()) return "redirect:/login";

        String uploadDir = "uploads/";
        String newFileName = System.currentTimeMillis() + "_" + file.getOriginalFilename();
        Path path = Paths.get(uploadDir, newFileName);
        Files.createDirectories(path.getParent());
        Files.copy(file.getInputStream(), path, StandardCopyOption.REPLACE_EXISTING);

        Note note = new Note();
        note.setTitle(title);
        note.setDescription(description);
        note.setFilePath(path.toString());
        note.setUploadDate(LocalDateTime.now());
        note.setUploader(optionalUser.get());

        noteRepository.save(note);

        return "redirect:/";
    }

    @GetMapping("/search")
    public String searchNotes(@RequestParam("query") String query, Model model, Principal principal) {
        if (principal == null) return "redirect:/login";

        Optional<User> optionalUser = userRepository.findByUsername(principal.getName());
        if (optionalUser.isEmpty()) return "redirect:/login";

        String corso = optionalUser.get().getCorsoDiStudi();
        List<Note> results = noteRepository.findByTitleContainingIgnoreCaseAndUploader_CorsoDiStudi(query, corso);

        model.addAttribute("results", results);
        model.addAttribute("query", query);
        return "searchResults";
    }
}
