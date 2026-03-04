package org.example.messagerie_association;

import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;
import org.example.messagerie_association.entity.Membre;
import org.example.messagerie_association.entity.Role;
import org.example.messagerie_association.entity.UserStatus;
import org.example.messagerie_association.network.ServerConnection;
import org.example.messagerie_association.server.Protocol;

import java.net.URL;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

public class ListeMembresController implements Initializable {

    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
    private static final DateTimeFormatter SERVER_FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    @FXML private TableView<Membre> tab_membres;
    @FXML private TableColumn<Membre, Integer> col_id;
    @FXML private TableColumn<Membre, String> col_username, col_nom, col_email, col_role, col_status, col_date;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        col_id.setCellValueFactory(new PropertyValueFactory<>("id"));
        col_username.setCellValueFactory(new PropertyValueFactory<>("username"));
        col_nom.setCellValueFactory(new PropertyValueFactory<>("nom"));
        col_email.setCellValueFactory(new PropertyValueFactory<>("email"));
        col_role.setCellValueFactory(cell -> new javafx.beans.property.SimpleStringProperty(
                cell.getValue().getRole() != null ? cell.getValue().getRole().name() : ""));
        col_status.setCellValueFactory(cell -> new javafx.beans.property.SimpleStringProperty(
                cell.getValue().getStatus() != null ? cell.getValue().getStatus().name() : ""));
        col_date.setCellValueFactory(cell -> new javafx.beans.property.SimpleStringProperty(
                cell.getValue().getDateCreation() != null ? cell.getValue().getDateCreation().format(FMT) : ""));
        try {
            List<String> lines = MessagerieApplication.getServerConnection().sendAndReadBlock(Protocol.GET_ALL_MEMBERS);
            List<Membre> membres = new ArrayList<>();
            for (String line : lines) {
                if (!line.startsWith(Protocol.PREFIX_MEMBER + Protocol.SEP)) continue;
                String[] p = line.split("\\" + Protocol.SEP, -1);
                if (p.length >= 6) {
                    Membre m = new Membre();
                    m.setId(Integer.parseInt(p[1].trim()));
                    m.setUsername(p[2].trim());
                    m.setNom(p[3].trim());
                    if (p.length >= 5) try { m.setRole(Role.valueOf(p[4].trim())); } catch (Exception ignored) {}
                    if (p.length >= 6) try { m.setStatus(UserStatus.valueOf(p[5].trim())); } catch (Exception ignored) {}
                    if (p.length >= 7 && !p[6].trim().isEmpty()) try { m.setDateCreation(LocalDateTime.parse(p[6].trim(), SERVER_FMT)); } catch (Exception ignored) {}
                    membres.add(m);
                }
            }
            tab_membres.setItems(FXCollections.observableArrayList(membres));
        } catch (Exception e) {
            tab_membres.setItems(FXCollections.observableArrayList());
        }
    }

    @FXML
    void fermer() {
        ((Stage) tab_membres.getScene().getWindow()).close();
    }
}
