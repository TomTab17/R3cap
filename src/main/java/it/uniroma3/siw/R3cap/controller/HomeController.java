package it.uniroma3.siw.R3cap.controller;

import it.uniroma3.siw.R3cap.model.Note;
import it.uniroma3.siw.R3cap.model.User;
import it.uniroma3.siw.R3cap.repository.UserRepository;
import it.uniroma3.siw.R3cap.repository.VoteRepository;
import it.uniroma3.siw.R3cap.repository.NoteRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.Optional;
import java.util.List;

@Controller
public class HomeController {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private NoteRepository noteRepository;

    @Autowired
    private VoteRepository voteRepository;

    @GetMapping("/")
    public String home(Model model, Principal principal) {
        if (principal != null) {
            String username = principal.getName();
            Optional<User> optionalUtente = userRepository.findByUsername(username);
            if (optionalUtente.isPresent()) {
                User utente = optionalUtente.get();
                model.addAttribute("utente", utente);
                model.addAttribute("corsoDiStudi", utente.getCorsoDiStudi());
            } else {
                model.addAttribute("utente", null);
            }
        } else {
            model.addAttribute("utente", null);
        }
        return "index";
    }

    @GetMapping("/profile")
public String profile(Model model, Principal principal) {
    if (principal != null) {
        String username = principal.getName();
        Optional<User> optionalUtente = userRepository.findByUsername(username);
        if (optionalUtente.isPresent()) {
            User utente = optionalUtente.get();
            model.addAttribute("utente", utente);

            // Punti totali per l'utente
            int totalPoints = voteRepository.findByNote_Uploader(utente)
                                            .stream()
                                            .mapToInt(v -> v.getValue())
                                            .sum();
            model.addAttribute("totalPoints", totalPoints);

            // Note caricate dall'utente
            List<Note> userNotes = noteRepository.findByUploader(utente);
            model.addAttribute("userNotes", userNotes);
        }
    }
    return "profile";
}

    @GetMapping("/profile/edit")
    public String editProfileForm(Model model, Principal principal) {
        if (principal != null) {
            Optional<User> optionalUtente = userRepository.findByUsername(principal.getName());
            optionalUtente.ifPresent(user -> model.addAttribute("utente", user));
        }
        return "editProfile";
    }

    @PostMapping("/profile/edit")
    public String editProfileSubmit(
            @RequestParam String nome,
            @RequestParam String cognome,
            @RequestParam String email,
            @RequestParam String corsoDiStudi,
            @RequestParam(required = false) boolean disponibileRipetizioni,
            @RequestParam(required = false) String immagineProfilo,
            Principal principal) {

        if (principal != null) {
            Optional<User> optionalUtente = userRepository.findByUsername(principal.getName());
            if (optionalUtente.isPresent()) {
                User user = optionalUtente.get();
                user.setNome(nome);
                user.setCognome(cognome);
                user.setEmail(email);
                user.setCorsoDiStudi(corsoDiStudi);
                user.setDisponibileRipetizioni(disponibileRipetizioni);
                user.setImmagineProfilo(immagineProfilo != null ? immagineProfilo : null);
                userRepository.save(user);
            }
        }
        return "redirect:/profile";
    }
}