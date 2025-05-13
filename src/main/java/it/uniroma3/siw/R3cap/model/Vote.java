package it.uniroma3.siw.R3cap.model;

import jakarta.persistence.*;

@Entity
public class Vote {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    private User voter;

    @ManyToOne
    private Note note;

    private int value; // 1 = upvote, -1 = downvote

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public User getVoter() { return voter; }
    public void setVoter(User voter) { this.voter = voter; }

    public Note getNote() { return note; }
    public void setNote(Note note) { this.note = note; }

    public int getValue() { return value; }
    public void setValue(int value) { this.value = value; }
}
