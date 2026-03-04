package org.example.messagerie_association;

import javafx.fxml.FXML;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import org.example.messagerie_association.entity.Membre;
import org.example.messagerie_association.entity.Role;
import org.example.messagerie_association.entity.UserStatus;
import org.example.messagerie_association.network.ServerConnection;
import org.example.messagerie_association.server.Protocol;
import org.example.messagerie_association.session.Session;
import org.example.messagerie_association.util.AlertHelper;

public class ConnexionController {

    @FXML private TextField tf_username;
    @FXML private PasswordField pf_password;

    @FXML
    void connexion() {
        String username = tf_username.getText().trim();
        String mdp = pf_password.getText();
        if (username.isEmpty() || mdp.isEmpty()) {
            AlertHelper.showWarning("Connexion", "Nom d'utilisateur et mot de passe obligatoires.");
            return;
        }
        ServerConnection conn = MessagerieApplication.getServerConnection();
        if (!conn.isConnected() && !conn.connect()) {
            AlertHelper.showError("Connexion", "Serveur indisponible. Démarrez le serveur (MessagerieServer).");
            return;
        }
        try {
            String response = conn.sendAndWait(Protocol.LOGIN + Protocol.SEP + username + Protocol.SEP + mdp);
            if (response.startsWith(Protocol.KO + Protocol.SEP)) {
                AlertHelper.showError("Connexion", response.substring(Protocol.KO.length() + 1).trim());
                return;
            }
            if (response.startsWith(Protocol.OK + Protocol.SEP)) {
                String[] parts = response.split("\\" + Protocol.SEP, -1);
                if (parts.length >= 5) {
                    int id = Integer.parseInt(parts[1].trim());
                    String un = parts[2].trim();
                    String nom = parts[3].trim();
                    String roleStr = parts[4].trim();
                    Role role;
                    try {
                        role = Role.valueOf(roleStr);
                    } catch (Exception e) {
                        role = Role.MEMBRE;
                    }
                    Membre m = new Membre();
                    m.setId(id);
                    m.setUsername(un);
                    m.setNom(nom);
                    m.setRole(role);
                    m.setStatus(UserStatus.ONLINE);
                    Session.setCurrentUser(m);
                    tf_username.clear();
                    pf_password.clear();
                    AlertHelper.showSuccess("Connexion", "Bienvenue " + m.getNom() + " !");
                    MessagerieApplication.showMessagerie();
                }
            }
        } catch (java.io.IOException e) {
            AlertHelper.showError("Connexion", "Serveur indisponible ou connexion perdue : " + e.getMessage());
        }
    }

    @FXML
    void versInscription() {
        MessagerieApplication.showInscription();
    }
}
