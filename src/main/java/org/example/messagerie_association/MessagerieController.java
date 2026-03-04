package org.example.messagerie_association;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.util.Duration;
import org.example.messagerie_association.entity.Membre;
import org.example.messagerie_association.entity.Message;
import org.example.messagerie_association.entity.Role;
import org.example.messagerie_association.entity.StatutMessage;
import org.example.messagerie_association.network.ServerConnection;
import org.example.messagerie_association.server.Protocol;
import org.example.messagerie_association.session.Session;
import org.example.messagerie_association.util.AlertHelper;

import java.net.URL;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

public class MessagerieController implements Initializable {

    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
    private static final DateTimeFormatter SERVER_DATE = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private static final int MAX_CONTENU = 1000;

    @FXML private Label label_connecte;
    @FXML private Button btn_deconnexion;
    @FXML private Button btn_liste_membres;
    @FXML private ListView<Membre> list_membres_connectes;
    @FXML private ComboBox<Membre> combo_destinataire;
    @FXML private TextArea ta_contenu;
    @FXML private Button btn_envoyer;
    @FXML private TableView<Message> tab_messages;
    @FXML private TableColumn<Message, Integer> col_msg_id;
    @FXML private TableColumn<Message, String> col_msg_contenu, col_msg_exp, col_msg_dest, col_msg_date, col_msg_statut;
    @FXML private TextField tf_recherche_message;

    private Timeline refreshTimeline;

    @FXML
    void deconnexion() {
        Membre current = Session.getCurrentUser();
        if (current != null) {
            try {
                MessagerieApplication.getServerConnection().sendAndWait(Protocol.LOGOUT + Protocol.SEP + current.getId());
            } catch (Exception ignored) {}
            Session.clear();
            if (refreshTimeline != null) refreshTimeline.stop();
            AlertHelper.showSuccess("Déconnexion", "Vous avez été déconnecté.");
            MessagerieApplication.showConnexion();
        }
    }

    @FXML
    void envoyerMessage(ActionEvent event) {
        Membre expediteur = Session.getCurrentUser();
        if (expediteur == null) {
            AlertHelper.showWarning("Envoi", "Vous devez être connecté (RG2).");
            return;
        }
        Membre dest = combo_destinataire.getSelectionModel().getSelectedItem();
        String contenu = ta_contenu.getText().trim().replace("|", " ");
        if (dest == null || contenu.isEmpty()) {
            AlertHelper.showWarning("Envoi", "Choisissez un destinataire et saisissez le message.");
            return;
        }
        if (contenu.length() > MAX_CONTENU) {
            AlertHelper.showWarning("Envoi", "Le message ne doit pas dépasser " + MAX_CONTENU + " caractères (RG7).");
            return;
        }
        if (expediteur.getId() == dest.getId()) {
            AlertHelper.showWarning("Envoi", "Vous ne pouvez pas vous envoyer un message à vous-même.");
            return;
        }
        try {
            String response = MessagerieApplication.getServerConnection().sendAndWait(
                    Protocol.SEND_MESSAGE + Protocol.SEP + dest.getId() + Protocol.SEP + contenu);
            if (response.startsWith(Protocol.KO + Protocol.SEP)) {
                AlertHelper.showError("Message", response.substring(Protocol.KO.length() + 1).trim());
                return;
            }
            ta_contenu.clear();
            loadMessages();
            AlertHelper.showSuccess("Message", "Message envoyé avec succès.");
        } catch (Exception e) {
            AlertHelper.showError("Message", "Erreur : " + e.getMessage());
        }
    }

    @FXML
    void ouvrirListeMembres() {
        Membre current = Session.getCurrentUser();
        if (current == null || current.getRole() != Role.ORGANISATEUR) {
            AlertHelper.showWarning("Accès", "Réservé aux ORGANISATEURS (RG13).");
            return;
        }
        MessagerieApplication.showListeMembres();
    }

    private void loadMembresConnectes() {
        try {
            List<String> lines = MessagerieApplication.getServerConnection().sendAndReadBlock(Protocol.GET_MEMBERS_ONLINE);
            List<Membre> membres = parseMembers(lines);
            list_membres_connectes.setItems(FXCollections.observableArrayList(membres));
        } catch (Exception e) {
            list_membres_connectes.setItems(FXCollections.observableArrayList());
        }
    }

    private void loadComboDestinataires() {
        Membre current = Session.getCurrentUser();
        if (current == null) return;
        try {
            List<String> lines = MessagerieApplication.getServerConnection().sendAndReadBlock(Protocol.GET_MEMBERS);
            List<Membre> tous = parseMembers(lines);
            tous.removeIf(m -> m.getId() == current.getId());
            combo_destinataire.setItems(FXCollections.observableArrayList(tous));
            combo_destinataire.setPromptText("Choisir un destinataire");
        } catch (Exception e) {
            combo_destinataire.setItems(FXCollections.observableArrayList());
        }
    }

    private List<Membre> parseMembers(List<String> lines) {
        List<Membre> list = new ArrayList<>();
        for (String line : lines) {
            if (!line.startsWith(Protocol.PREFIX_MEMBER + Protocol.SEP)) continue;
            String[] p = line.split("\\" + Protocol.SEP, -1);
            if (p.length >= 4) {
                Membre m = new Membre();
                m.setId(Integer.parseInt(p[1].trim()));
                m.setUsername(p[2].trim());
                m.setNom(p[3].trim());
                if (p.length >= 5) try { m.setRole(Role.valueOf(p[4].trim())); } catch (Exception ignored) {}
                list.add(m);
            }
        }
        return list;
    }

    private void loadMessages() {
        try {
            List<String> lines = MessagerieApplication.getServerConnection().sendAndReadBlock(Protocol.GET_MESSAGES);
            List<Message> messages = parseMessages(lines);
            ObservableList<Message> list = FXCollections.observableArrayList(messages);
            col_msg_id.setCellValueFactory(new PropertyValueFactory<>("id"));
            col_msg_contenu.setCellValueFactory(new PropertyValueFactory<>("contenu"));
            col_msg_exp.setCellValueFactory(cell -> new javafx.beans.property.SimpleStringProperty(cell.getValue().getExpediteur() != null ? cell.getValue().getExpediteur().getNom() : ""));
            col_msg_dest.setCellValueFactory(cell -> new javafx.beans.property.SimpleStringProperty(cell.getValue().getDestinataire() != null ? cell.getValue().getDestinataire().getNom() : ""));
            col_msg_date.setCellValueFactory(cell -> new javafx.beans.property.SimpleStringProperty(cell.getValue().getDateEnvoi() != null ? cell.getValue().getDateEnvoi().format(DATE_FORMAT) : ""));
            col_msg_statut.setCellValueFactory(cell -> new javafx.beans.property.SimpleStringProperty(cell.getValue().getStatut() != null ? cell.getValue().getStatut().name() : ""));

            FilteredList<Message> filtered = new FilteredList<>(list, p -> true);
            tf_recherche_message.textProperty().addListener((obs, oldV, newV) -> {
                filtered.setPredicate(msg -> {
                    if (newV == null || newV.isEmpty()) return true;
                    String f = newV.toLowerCase();
                    return (msg.getContenu() != null && msg.getContenu().toLowerCase().contains(f))
                            || (msg.getExpediteur() != null && msg.getExpediteur().getNom().toLowerCase().contains(f))
                            || (msg.getDestinataire() != null && msg.getDestinataire().getNom().toLowerCase().contains(f));
                });
            });
            SortedList<Message> sorted = new SortedList<>(filtered);
            sorted.comparatorProperty().bind(tab_messages.comparatorProperty());
            tab_messages.setItems(sorted);
        } catch (Exception e) {
            tab_messages.setItems(FXCollections.observableArrayList());
        }
    }

    private List<Message> parseMessages(List<String> lines) {
        List<Message> list = new ArrayList<>();
        for (String line : lines) {
            if (!line.startsWith(Protocol.PREFIX_MSG + Protocol.SEP)) continue;
            String[] p = line.split("\\" + Protocol.SEP, -1);
            if (p.length >= 9) {
                Message msg = new Message();
                msg.setId(Integer.parseInt(p[1].trim()));
                Membre exp = new Membre();
                exp.setId(Integer.parseInt(p[2].trim()));
                exp.setNom(p[3].trim());
                Membre dest = new Membre();
                dest.setId(Integer.parseInt(p[4].trim()));
                dest.setNom(p[5].trim());
                msg.setContenu(p[6].trim());
                try { msg.setDateEnvoi(LocalDateTime.parse(p[7].trim(), SERVER_DATE)); } catch (Exception ignored) {}
                try { msg.setStatut(StatutMessage.valueOf(p[8].trim())); } catch (Exception ignored) {}
                msg.setExpediteur(exp);
                msg.setDestinataire(dest);
                list.add(msg);
            }
        }
        return list;
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        Membre current = Session.getCurrentUser();
        if (current == null) {
            MessagerieApplication.showConnexion();
            return;
        }
        label_connecte.setText("Connecté : " + current.getNom() + " (" + current.getRoleLibelle() + ")");
        btn_liste_membres.setVisible(current.getRole() == Role.ORGANISATEUR);
        btn_liste_membres.setManaged(current.getRole() == Role.ORGANISATEUR);

        loadMembresConnectes();
        loadComboDestinataires();
        loadMessages();

        refreshTimeline = new Timeline(new KeyFrame(Duration.seconds(4), e -> {
            loadMembresConnectes();
            loadMessages();
        }));
        refreshTimeline.setCycleCount(Timeline.INDEFINITE);
        refreshTimeline.play();
    }
}
