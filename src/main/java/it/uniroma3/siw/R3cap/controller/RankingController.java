package it.uniroma3.siw.R3cap.controller;

import it.uniroma3.siw.R3cap.model.User;
import it.uniroma3.siw.R3cap.repository.UserRepository;
import it.uniroma3.siw.R3cap.repository.VoteRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@Controller
@RequestMapping("/ranking")
public class RankingController {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private VoteRepository voteRepository;

    @GetMapping
    public String showRanking(@RequestParam(name = "corsoDiStudi", required = false) String corso, Model model) {
        List<User> allUsers = userRepository.findAll();
        List<UserScore> ranking = new ArrayList<>();

        for (User u : allUsers) {
            int score = (corso == null || corso.equals("Tutti")) ?
                voteRepository.findByNote_Uploader(u).stream().mapToInt(v -> v.getValue()).sum() :
                voteRepository.findByNote_UploaderAndNote_Uploader_CorsoDiStudi(u, corso).stream().mapToInt(v -> v.getValue()).sum();

            // Escludi gli utenti con 0 punti
            if (score > 0) {
                ranking.add(new UserScore(u, score));
            }
        }

        // Ordina e prendi i primi 10
        ranking.sort((a, b) -> Integer.compare(b.score, a.score));
        model.addAttribute("ranking", ranking.subList(0, Math.min(10, ranking.size())));
        model.addAttribute("corsoDiStudi", corso != null ? corso : "Tutti");
        return "ranking";
    }

    public static class UserScore {
        public User user;
        public int score;

        public UserScore(User user, int score) {
            this.user = user;
            this.score = score;
        }
    }
}
