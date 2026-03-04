package org.example.messagerie_association;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.ComboBox;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import org.example.messagerie_association.entity.Role;
import org.example.messagerie_association.network.ServerConnection;
import org.example.messagerie_association.server.Protocol;
import org.example.messagerie_association.util.AlertHelper;

import java.net.URL;
import java.util.ResourceBundle;

public class InscriptionController implements Initializable {

    @FXML private TextField tf_username, tf_nom;
    @FXML private PasswordField pf_password;
    @FXML private ComboBox<Role> combo_role;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        combo_role.getItems().setAll(Role.ORGANISATEUR, Role.MEMBRE, Role.BENEVOLE);
        combo_role.setConverter(new javafx.util.StringConverter<Role>() {
            @Override
            public String toString(Role r) {
                return r == null ? "" : r.name();
            }
            @Override
            public Role fromString(String s) {
                if (s == null || s.isEmpty()) return null;
                try {
                    return Role.valueOf(s);
                } catch (Exception e) {
                    return null;
                }
            }
        });
    }

    @FXML
    void inscription() {
        String username = tf_username.getText().trim();
        String nom = tf_nom.getText().trim();
        String mdp = pf_password.getText();
        Role role = combo_role.getValue();
        if (username.isEmpty() || nom.isEmpty() || mdp.isEmpty()) {
            AlertHelper.showWarning("Inscription", "Nom d'utilisateur, nom et mot de passe obligatoires.");
            return;
        }
        if (role == null) {
            AlertHelper.showWarning("Inscription", "Veuillez sélectionner un rôle.");
            return;
        }
        ServerConnection conn = MessagerieApplication.getServerConnection();
        if (!conn.isConnected() && !conn.connect()) {
            AlertHelper.showError("Inscription", "Serveur indisponible.");
            return;
        }
        try {
            String line = Protocol.INSCRIRE + Protocol.SEP + username + Protocol.SEP + nom + Protocol.SEP + mdp + Protocol.SEP + role.name();
            String response = conn.sendAndWait(line);
            if (response.startsWith(Protocol.KO + Protocol.SEP)) {
                AlertHelper.showError("Inscription", response.substring(Protocol.KO.length() + 1).trim());
                return;
            }
            tf_username.clear();
            tf_nom.clear();
            pf_password.clear();
            combo_role.setValue(null);
            AlertHelper.showSuccess("Inscription", "Compte créé. Vous pouvez vous connecter.");
            MessagerieApplication.showConnexion();
        } catch (Exception e) {
            AlertHelper.showError("Inscription", "Erreur : " + e.getMessage());
        }
    }

    @FXML
    void retour() {
        MessagerieApplication.showConnexion();
    }
}
