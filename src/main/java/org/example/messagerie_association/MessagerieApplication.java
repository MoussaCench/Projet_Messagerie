package org.example.messagerie_association;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.stage.Modality;
import javafx.stage.Stage;
import org.example.messagerie_association.network.ServerConnection;
import org.example.messagerie_association.session.Session;
import org.example.messagerie_association.util.AlertHelper;

import java.io.IOException;

public class MessagerieApplication extends Application {

    private static Stage primaryStage;
    private static final ServerConnection serverConnection = new ServerConnection();

    public static ServerConnection getServerConnection() {
        return serverConnection;
    }

    @Override
    public void start(Stage stage) throws IOException {
        primaryStage = stage;
        primaryStage.setTitle("Projet G2 - Messagerie Association");
        primaryStage.setMinWidth(500);
        primaryStage.setMinHeight(400);

        serverConnection.setOnDisconnect(() -> {
            AlertHelper.showError("Connexion", "Connexion au serveur perdue (RG10). Vous êtes déconnecté.");
            Session.clear();
            showConnexion();
        });

        if (!serverConnection.connect()) {
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle("Serveur indisponible");
            alert.setHeaderText("Impossible de se connecter au serveur");
            alert.setContentText(
                "Démarrez d'abord le serveur (classe MessagerieServer) sur le port " + ServerConnection.getPort() + ".\n" +
                "Vous pouvez réessayer en cliquant sur « Se connecter »."
            );
            alert.showAndWait();
        }

        primaryStage.setOnCloseRequest(e -> {
            if (Session.isLoggedIn()) {
                try {
                    serverConnection.sendAndWait("LOGOUT|" + Session.getCurrentUser().getId());
                } catch (Exception ignored) {}
                Session.clear();
            }
            serverConnection.disconnect();
        });
        showConnexion();
        primaryStage.show();
    }

    private static void applyStyles(Scene scene) {
        String css = MessagerieApplication.class.getResource("styles.css").toExternalForm();
        if (css != null) scene.getStylesheets().setAll(css);
    }

    public static void showConnexion() {
        try {
            FXMLLoader loader = new FXMLLoader(MessagerieApplication.class.getResource("connexion-view.fxml"));
            Scene scene = new Scene(loader.load(), 420, 420);
            applyStyles(scene);
            primaryStage.setScene(scene);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static void showInscription() {
        try {
            FXMLLoader loader = new FXMLLoader(MessagerieApplication.class.getResource("inscription-view.fxml"));
            Scene scene = new Scene(loader.load(), 440, 520);
            applyStyles(scene);
            primaryStage.setScene(scene);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static void showMessagerie() {
        try {
            FXMLLoader loader = new FXMLLoader(MessagerieApplication.class.getResource("messagerie-view.fxml"));
            Scene scene = new Scene(loader.load(), 940, 740);
            applyStyles(scene);
            primaryStage.setScene(scene);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static void showListeMembres() {
        try {
            FXMLLoader loader = new FXMLLoader(MessagerieApplication.class.getResource("liste-membres-view.fxml"));
            Stage stage = new Stage();
            stage.initModality(Modality.NONE);
            stage.setTitle("Liste des membres inscrits (RG13)");
            stage.setScene(new Scene(loader.load(), 720, 480));
            applyStyles(stage.getScene());
            stage.show();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
