package org.example.messagerie_association.server;

import org.example.messagerie_association.database.DatabaseManager;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MessagerieServer {

    public static final int DEFAULT_PORT = 9000;

    public static void main(String[] args) {
        int port = args.length > 0 ? Integer.parseInt(args[0]) : DEFAULT_PORT;
        System.out.println("Initialisation de la base de données...");
        try {
            DatabaseManager.getEntityManagerFactory();
        } catch (Exception e) {
            System.err.println("Impossible de se connecter à la base : " + e.getMessage());
            return;
        }
        ExecutorService executor = Executors.newCachedThreadPool();
        try (ServerSocket serverSocket = new ServerSocket(port)) {
            System.out.println("Serveur démarré sur le port " + port + ". En attente de clients...");
            while (true) {
                Socket clientSocket = serverSocket.accept();
                executor.submit(new ClientHandler(clientSocket));
            }
        } catch (IOException e) {
            System.err.println("Erreur serveur : " + e.getMessage());
        } finally {
            executor.shutdown();
        }
    }
}
