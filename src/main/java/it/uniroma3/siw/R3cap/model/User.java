package it.uniroma3.siw.R3cap.model;

import jakarta.persistence.*;

@Entity
@Table(name = "users")  // "user" è riservato in alcuni DB (come PostgreSQL)
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String username;
    private String password;
    private String email;
    private String nome;
    private String cognome;
    private String corsoDiStudi;
    private boolean disponibileRipetizioni;  // Flag per disponibilità ripetizioni
    private String role = "USER";  // ruolo di default

    // Getters e setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getNome() { return nome; }
    public void setNome(String nome) { this.nome = nome; }

    public String getCognome() { return cognome; }
    public void setCognome(String cognome) { this.cognome = cognome; }

    public String getCorsoDiStudi() { return corsoDiStudi; }
    public void setCorsoDiStudi(String corsoDiStudi) { this.corsoDiStudi = corsoDiStudi; }

    public boolean isDisponibileRipetizioni() { return disponibileRipetizioni; }
    public void setDisponibileRipetizioni(boolean disponibileRipetizioni) { this.disponibileRipetizioni = disponibileRipetizioni; }

    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }
}