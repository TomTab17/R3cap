package it.uniroma3.siw.R3cap.controller;

import it.uniroma3.siw.R3cap.model.Note;
import it.uniroma3.siw.R3cap.model.User;
import it.uniroma3.siw.R3cap.model.Vote;
import it.uniroma3.siw.R3cap.repository.NoteRepository;
import it.uniroma3.siw.R3cap.repository.UserRepository;
import it.uniroma3.siw.R3cap.repository.VoteRepository;
import it.uniroma3.siw.R3cap.service.PdfPreviewGenerator;
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
import java.util.*;

@Controller
@RequestMapping("/notes")
public class NoteController {

    @Autowired
    private NoteRepository noteRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private VoteRepository voteRepository;

    private final String uploadDir = "uploads/";
    private final String previewDir = "src/main/resources/static/previews/";

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
        Files.createDirectories(Paths.get(previewDir));

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

        try {
            String previewPath = PdfPreviewGenerator.generatePreview(
                path.toFile(),
                previewDir,
                String.valueOf(note.getId())
            );
            note.setPreviewImagePath(previewPath);
            noteRepository.save(note);
        } catch (Exception e) {
            System.err.println("Errore durante la generazione della preview: " + e.getMessage());
        }

        return "redirect:/?uploadSuccess=true";
    }

    @GetMapping("/search")
    public String searchNotes(@RequestParam("query") String query, Model model, Principal principal) {
        Optional<User> optionalUser = getAuthenticatedUser(principal);
        if (optionalUser.isEmpty()) return "redirect:/login";

        String corso = optionalUser.get().getCorsoDiStudi();
        List<Note> results = noteRepository.findByTitleContainingIgnoreCaseAndUploader_CorsoDiStudi(query, corso);

        model.addAttribute("results", results);
        model.addAttribute("query", query);
        model.addAttribute("corsoDiStudiUtente", corso);

        // Recupero voti dell'utente
        User user = optionalUser.get();
        Map<Long, Integer> votesMap = new HashMap<>();
        for (Note note : results) {
            voteRepository.findByVoterAndNote(user, note).ifPresent(vote -> votesMap.put(note.getId(), vote.getValue()));
        }
        model.addAttribute("votes", votesMap);

        return "searchResults";
    }

    @GetMapping("/ricerca-avanzata")
    public String showAdvancedSearchForm(Principal principal, Model model) {
        if (principal == null) return "redirect:/login";
        return "advancedSearch";
    }

    @GetMapping("/risultati-ricerca-avanzata")
    public String advancedSearchNotes(@RequestParam("query") String query,
                                      @RequestParam("corsoDiStudi") String corsoDiStudi,
                                      Model model, Principal principal) {
        Optional<User> optionalUser = getAuthenticatedUser(principal);
        if (optionalUser.isEmpty()) return "redirect:/login";

        List<Note> results;

        if ("Tutti".equals(corsoDiStudi)) {
            results = noteRepository.findByTitleContainingIgnoreCase(query);
        } else {
            results = noteRepository.findByTitleContainingIgnoreCaseAndUploader_CorsoDiStudiIgnoreCase(query, corsoDiStudi);
        }

        model.addAttribute("results", results);
        model.addAttribute("query", query);
        model.addAttribute("corsoDiStudiSelezionato", corsoDiStudi);

        // Recupero voti dell'utente
        User user = optionalUser.get();
        Map<Long, Integer> votesMap = new HashMap<>();
        for (Note note : results) {
            voteRepository.findByVoterAndNote(user, note).ifPresent(vote -> votesMap.put(note.getId(), vote.getValue()));
        }
        model.addAttribute("votes", votesMap);

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

    @PostMapping("/vote")
    @ResponseBody
    public String voteNote(@RequestParam Long noteId,
                           @RequestParam int value,
                           Principal principal) {
        Optional<User> userOpt = getAuthenticatedUser(principal);
        Optional<Note> noteOpt = noteRepository.findById(noteId);

        if (userOpt.isPresent() && noteOpt.isPresent()) {
            User user = userOpt.get();
            Note note = noteOpt.get();

            Optional<Vote> existingVote = voteRepository.findByVoterAndNote(user, note);
            if (existingVote.isPresent()) {
                existingVote.get().setValue(value);
                voteRepository.save(existingVote.get());
            } else {
                Vote vote = new Vote();
                vote.setNote(note);
                vote.setVoter(user);
                vote.setValue(value);
                voteRepository.save(vote);
            }
            return "OK";
        }

        return "ERROR";
    }

    @GetMapping("/edit/{id}")
public String editNoteForm(@PathVariable Long id, Model model, Principal principal) {
    Optional<Note> noteOpt = noteRepository.findById(id);
    Optional<User> userOpt = getAuthenticatedUser(principal);

    if (noteOpt.isEmpty() || userOpt.isEmpty()) {
        return "redirect:/profile";
    }

    Note note = noteOpt.get();
    User user = userOpt.get();

    // Controllo che la nota appartenga all'utente loggato
    if (!note.getUploader().getId().equals(user.getId())) {
        return "redirect:/profile";
    }

    model.addAttribute("note", note);
    return "editNote"; 
}
@PostMapping("/edit/{id}")
public String editNoteSubmit(@PathVariable Long id,
                             @RequestParam String title,
                             @RequestParam String description,
                             @RequestParam(required = false) MultipartFile file,
                             Principal principal) throws IOException {

    Optional<Note> noteOpt = noteRepository.findById(id);
    Optional<User> userOpt = getAuthenticatedUser(principal);

    if (noteOpt.isEmpty() || userOpt.isEmpty()) {
        return "redirect:/profile";
    }

    Note note = noteOpt.get();
    User user = userOpt.get();

    if (!note.getUploader().getId().equals(user.getId())) {
        return "redirect:/profile";
    }

    note.setTitle(title);
    note.setDescription(description);

    if (file != null && !file.isEmpty()) {
        // Sovrascrivo il file e aggiorno il path
        Files.createDirectories(Paths.get(uploadDir));
        String originalFilename = Paths.get(file.getOriginalFilename()).getFileName().toString();
        String sanitizedFilename = originalFilename.replaceAll("[^a-zA-Z0-9\\.\\-_]", "_");
        String newFileName = UUID.randomUUID() + "_" + sanitizedFilename;
        Path path = Paths.get(uploadDir, newFileName);

        Files.copy(file.getInputStream(), path, StandardCopyOption.REPLACE_EXISTING);
        note.setFilePath(path.toString());

        try {
            String previewPath = PdfPreviewGenerator.generatePreview(
                path.toFile(),
                previewDir,
                String.valueOf(note.getId())
            );
            note.setPreviewImagePath(previewPath);
        } catch (Exception e) {
            System.err.println("Errore durante la generazione della preview: " + e.getMessage());
        }
    }

    noteRepository.save(note);
    return "redirect:/profile";
}
@PostMapping("/delete/{id}")
public String deleteNote(@PathVariable Long id, Principal principal) {
    Optional<Note> noteOpt = noteRepository.findById(id);
    Optional<User> userOpt = getAuthenticatedUser(principal);

    if (noteOpt.isEmpty() || userOpt.isEmpty()) {
        return "redirect:/profile";
    }

    Note note = noteOpt.get();
    User user = userOpt.get();

    if (!note.getUploader().getId().equals(user.getId())) {
        return "redirect:/profile";
    }

    // Elimino i voti associati
    List<Vote> votes = voteRepository.findByNote(note);
    voteRepository.deleteAll(votes);

    // Elimino il file PDF fisico
    try {
        Path pdfPath = Paths.get(note.getFilePath());
        Files.deleteIfExists(pdfPath);
    } catch (IOException e) {
        System.err.println("Errore eliminando file PDF: " + e.getMessage());
    }

    // Elimino l'immagine di preview fisica
    try {
        if (note.getPreviewImagePath() != null) {
            String previewFileName = note.getPreviewImagePath().replaceFirst("^/", "");
            Path previewPath = Paths.get("src/main/resources/static", previewFileName);
            Files.deleteIfExists(previewPath);
        }
    } catch (IOException e) {
        System.err.println("Errore eliminando immagine preview: " + e.getMessage());
    }

    // Elimino la nota dal DB
    noteRepository.delete(note);

    return "redirect:/profile";
}

}