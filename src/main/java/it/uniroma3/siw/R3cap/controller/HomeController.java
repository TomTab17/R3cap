package it.uniroma3.siw.R3cap.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class HomeController {

    @GetMapping("/")
    public String home() {
        return "index"; // Cercherà index.html in templates/
    }

    @GetMapping("/upload.html")
    public String uploadPage() {
        return "upload"; // Cercherà upload.html
    }
}
