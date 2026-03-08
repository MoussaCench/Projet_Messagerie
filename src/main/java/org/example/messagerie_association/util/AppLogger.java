package org.example.messagerie_association.util;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public final class AppLogger {

    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private static Path logFile;

    static {
        try {
            logFile = Path.of(System.getProperty("user.home"), "messagerie_association.log");
        } catch (Exception e) {
            logFile = Path.of("messagerie_association.log");
        }
    }

    public static void logConnexion(String username) {
        log("CONNEXION", username, null, "Utilisateur connecté");
    }

    public static void logDeconnexion(String username) {
        log("DECONNEXION", username, null, "Utilisateur déconnecté");
    }

    public static void logEnvoiMessage(String expediteur, String destinataire, int messageId) {
        log("MESSAGE_ENVOYE", expediteur, destinataire, "Message #" + messageId);
    }

    private static void log(String type, String user1, String user2, String detail) {
        String line = String.format("[%s] %s | %s | %s | %s%n",
                LocalDateTime.now().format(FMT),
                type,
                user1 != null ? user1 : "-",
                user2 != null ? user2 : "-",
                detail);
        try {
            Files.writeString(logFile, line, StandardOpenOption.CREATE, StandardOpenOption.APPEND);
        } catch (IOException ignored) {
        }
    }
}
