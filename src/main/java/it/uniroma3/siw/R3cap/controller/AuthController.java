package it.uniroma3.siw.R3cap.controller;

import it.uniroma3.siw.R3cap.model.User;
import it.uniroma3.siw.R3cap.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

@Controller
public class AuthController {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private BCryptPasswordEncoder encoder;

    @GetMapping("/login")
    public String login() {
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
            @RequestParam(name = "disponibileRipetizioni", defaultValue = "false") boolean disponibileRipetizioni) {

        User user = new User();
        user.setUsername(username);
        user.setPassword(encoder.encode(password));  // Cripta la password
        user.setEmail(email);
        user.setNome(nome);
        user.setCognome(cognome);
        user.setCorsoDiStudi(corsoDiStudi);
        user.setDisponibileRipetizioni(disponibileRipetizioni);
        user.setRole("USER");

        System.out.println("Registrazione utente: " + user.getUsername()); // Debug

        userRepository.save(user);

        return "redirect:/login?success";
    }
}
