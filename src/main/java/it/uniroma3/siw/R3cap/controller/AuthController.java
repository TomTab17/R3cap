package it.uniroma3.siw.R3cap.controller;

import it.uniroma3.siw.R3cap.model.User;
import it.uniroma3.siw.R3cap.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
public class AuthController {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private BCryptPasswordEncoder encoder;

    @GetMapping("/login")
    public String login(@RequestParam(value = "success", required = false) String success, Model model) {
        if (success != null) {
            model.addAttribute("successMessage", "Registrazione avvenuta con successo! Effettua il login.");
        }
        return "login"; // templates/login.html
    }

    @GetMapping("/register")
    public String registerForm() {
        return "register"; // templates/register.html
    }

    @PostMapping("/register")
    public String register(
            @RequestParam String nome,
            @RequestParam String cognome,
            @RequestParam String username,
            @RequestParam String email,
            @RequestParam String password,
            @RequestParam String corsoDiStudi,
            @RequestParam(name = "disponibileRipetizioni", defaultValue = "false") boolean disponibileRipetizioni,
            @RequestParam(name = "immagineProfilo", required = false) String immagineProfilo,
            Model model) {

        if (userRepository.findByUsername(username).isPresent()) {
            model.addAttribute("error", "Username già esistente.");
            return "register";
        }

        if (userRepository.findByEmail(email).isPresent()) {
            model.addAttribute("error", "Email già registrata.");
            return "register";
        }

        User user = new User();
        user.setUsername(username);
        user.setPassword(encoder.encode(password));
        user.setEmail(email);
        user.setNome(nome);
        user.setCognome(cognome);
        user.setCorsoDiStudi(corsoDiStudi);
        user.setDisponibileRipetizioni(disponibileRipetizioni);
        user.setImmagineProfilo(immagineProfilo);
        user.setRole("USER");

        userRepository.save(user);

        return "redirect:/login?success";
    }
}
