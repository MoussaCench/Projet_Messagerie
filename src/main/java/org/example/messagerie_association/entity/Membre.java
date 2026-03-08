package org.example.messagerie_association.entity;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "membre")
public class Membre {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @Column(name = "username", unique = true, nullable = false, length = 100)
    private String username;

    @Column(name = "nom", nullable = false, length = 200)
    private String nom;

    @Column(name = "email", length = 255)
    private String email;

    @Column(name = "mot_de_passe", nullable = false, length = 100)
    private String motDePasse;

    @Enumerated(EnumType.STRING)
    @Column(name = "role", nullable = false)
    private Role role;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private UserStatus status = UserStatus.OFFLINE;

    @Column(name = "date_creation")
    private LocalDateTime dateCreation;

    @OneToMany(mappedBy = "expediteur", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Message> messagesEnvoyes = new ArrayList<>();

    public Membre() {
        this.status = UserStatus.OFFLINE;
        this.dateCreation = LocalDateTime.now();
    }

    public Membre(String username, String nom, String motDePasse, Role role) {
        this.username = username;
        this.nom = nom;
        this.motDePasse = motDePasse != null ? motDePasse : "";
        this.role = role != null ? role : Role.MEMBRE;
        this.status = UserStatus.OFFLINE;
        this.dateCreation = LocalDateTime.now();
    }

    @PrePersist
    public void prePersist() {
        if (dateCreation == null) dateCreation = LocalDateTime.now();
        if (status == null) status = UserStatus.OFFLINE;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getNom() { return nom; }
    public void setNom(String nom) { this.nom = nom; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getMotDePasse() { return motDePasse; }
    public void setMotDePasse(String motDePasse) { this.motDePasse = motDePasse; }

    public Role getRole() { return role; }
    public void setRole(Role role) { this.role = role; }

    public UserStatus getStatus() { return status; }
    public void setStatus(UserStatus status) { this.status = status != null ? status : UserStatus.OFFLINE; }

    public boolean isEnLigne() { return status == UserStatus.ONLINE; }
    public void setEnLigne(boolean enLigne) { this.status = enLigne ? UserStatus.ONLINE : UserStatus.OFFLINE; }

    public LocalDateTime getDateCreation() { return dateCreation; }
    public void setDateCreation(LocalDateTime dateCreation) { this.dateCreation = dateCreation; }

    public String getRoleLibelle() { return role != null ? role.name() : ""; }

    @Override
    public String toString() {
        return nom + " (" + username + ")";
    }
}
