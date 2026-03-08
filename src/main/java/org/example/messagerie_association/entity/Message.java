package org.example.messagerie_association.entity;

import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "message")
public class Message {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "expediteur_id", nullable = false)
    private Membre expediteur;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "destinataire_id", nullable = false)
    private Membre destinataire;

    @Column(name = "contenu", nullable = false, length = 1000)
    private String contenu;

    @Column(name = "date_envoi")
    private LocalDateTime dateEnvoi;

    @Enumerated(EnumType.STRING)
    @Column(name = "statut", nullable = false)
    private StatutMessage statut = StatutMessage.ENVOYE;

    public Message() {
        this.dateEnvoi = LocalDateTime.now();
        this.statut = StatutMessage.ENVOYE;
    }

    public Message(String contenu, Membre expediteur, Membre destinataire) {
        this.contenu = contenu;
        this.expediteur = expediteur;
        this.destinataire = destinataire;
        this.dateEnvoi = LocalDateTime.now();
        this.statut = StatutMessage.ENVOYE;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public Membre getExpediteur() { return expediteur; }
    public void setExpediteur(Membre expediteur) { this.expediteur = expediteur; }

    public Membre getDestinataire() { return destinataire; }
    public void setDestinataire(Membre destinataire) { this.destinataire = destinataire; }

    public String getContenu() { return contenu; }
    public void setContenu(String contenu) { this.contenu = contenu; }

    public LocalDateTime getDateEnvoi() { return dateEnvoi; }
    public void setDateEnvoi(LocalDateTime dateEnvoi) { this.dateEnvoi = dateEnvoi; }

    public StatutMessage getStatut() { return statut; }
    public void setStatut(StatutMessage statut) { this.statut = statut != null ? statut : StatutMessage.ENVOYE; }

    public boolean isLu() { return statut == StatutMessage.LU; }
    public void setLu(boolean lu) { this.statut = lu ? StatutMessage.LU : this.statut; }
}
