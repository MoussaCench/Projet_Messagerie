package org.example.messagerie_association.util;

import javafx.scene.control.Alert;

public final class AlertHelper {

    private AlertHelper() {
    }

    public static void showSuccess(String titre, String message) {
        Alert a = new Alert(Alert.AlertType.INFORMATION);
        a.setTitle(titre);
        a.setHeaderText("Succès");
        a.setContentText(message);
        a.showAndWait();
    }

    public static void showError(String titre, String message) {
        Alert a = new Alert(Alert.AlertType.ERROR);
        a.setTitle(titre);
        a.setHeaderText("Erreur");
        a.setContentText(message);
        a.showAndWait();
    }

    public static void showWarning(String titre, String message) {
        Alert a = new Alert(Alert.AlertType.WARNING);
        a.setTitle(titre);
        a.setHeaderText("Attention");
        a.setContentText(message);
        a.showAndWait();
    }
}
