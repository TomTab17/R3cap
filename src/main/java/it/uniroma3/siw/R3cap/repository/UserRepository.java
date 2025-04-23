package it.uniroma3.siw.R3cap.repository;

import it.uniroma3.siw.R3cap.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, Long> {
    User findByUsername(String username);
}
