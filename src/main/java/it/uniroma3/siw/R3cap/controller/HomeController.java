package it.uniroma3.siw.R3cap.controller;

import it.uniroma3.siw.R3cap.model.User;
import it.uniroma3.siw.R3cap.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.security.Principal;
import java.util.Optional;

@Controller
public class HomeController {

    @Autowired
    private UserRepository userRepository;

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
            model.addAttribute("utente", null); // Se non loggato, utente è null
        }
        return "index";
    }

    @GetMapping("/upload.html")
    public String uploadPage() {
        return "upload";
    }
}
