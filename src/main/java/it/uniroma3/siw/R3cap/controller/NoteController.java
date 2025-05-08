package it.uniroma3.siw.R3cap.controller;

import it.uniroma3.siw.R3cap.model.Note;
import it.uniroma3.siw.R3cap.model.User;
import it.uniroma3.siw.R3cap.repository.NoteRepository;
import it.uniroma3.siw.R3cap.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.net.MalformedURLException;
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

    private final String uploadDir = "uploads/";

    private Optional<User> getAuthenticatedUser(Principal principal) {
        if (principal == null) return Optional.empty();
        return userRepository.findByUsername(principal.getName());
    }

    @GetMapping("/upload")
    public String showUploadForm(Principal principal) {
        if (principal == null) return "redirect:/login";
        return "upload";
    }

    @PostMapping("/upload")
    public String uploadNote(
            @RequestParam("title") String title,
            @RequestParam("description") String description,
            @RequestParam("file") MultipartFile file,
            Principal principal,
            Model model
    ) throws IOException {

        if (file.isEmpty()) {
            model.addAttribute("error", "Il file non può essere vuoto.");
            return "upload";
        }

        Optional<User> optionalUser = getAuthenticatedUser(principal);
        if (optionalUser.isEmpty()) return "redirect:/login";

        Files.createDirectories(Paths.get(uploadDir));

        String originalFilename = Paths.get(file.getOriginalFilename()).getFileName().toString();
        String sanitizedFilename = originalFilename.replaceAll("[^a-zA-Z0-9\\.\\-_]", "_");
        String newFileName = UUID.randomUUID() + "_" + sanitizedFilename;
        Path path = Paths.get(uploadDir, newFileName);

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
        Optional<User> optionalUser = getAuthenticatedUser(principal);
        if (optionalUser.isEmpty()) return "redirect:/login";

        String corso = optionalUser.get().getCorsoDiStudi();
        List<Note> results = noteRepository.findByTitleContainingIgnoreCaseAndUploader_CorsoDiStudi(query, corso);

        model.addAttribute("results", results);
        model.addAttribute("query", query);
        return "searchResults";
    }

    @GetMapping("/download/{noteId}")
    public ResponseEntity<Resource> downloadNote(@PathVariable Long noteId) throws MalformedURLException {
        Optional<Note> optionalNote = noteRepository.findById(noteId);
        if (optionalNote.isEmpty()) return ResponseEntity.notFound().build();

        Note note = optionalNote.get();
        Path path = Paths.get(note.getFilePath());
        Resource resource = new UrlResource(path.toUri());

        if (!resource.exists() || !resource.isReadable()) {
            return ResponseEntity.badRequest().build();
        }

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + path.getFileName().toString() + "\"")
                .body(resource);
    }
}
