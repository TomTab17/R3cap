package it.uniroma3.siw.R3cap.controller;

import it.uniroma3.siw.R3cap.model.Note;
import it.uniroma3.siw.R3cap.repository.NoteRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.*;
import java.time.LocalDateTime;

@Controller
@RequestMapping("/notes")
public class NoteController {

    @Autowired
    private NoteRepository noteRepository;

    @PostMapping("/upload")
    public String uploadNote(
            @RequestParam("title") String title,
            @RequestParam("description") String description,
            @RequestParam("file") MultipartFile file
    ) throws IOException {

        if (file.isEmpty()) return "redirect:/?error=file";

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

        noteRepository.save(note);

        return "redirect:/";
    }
}
