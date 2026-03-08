package org.example.messagerie_association.server;

import org.example.messagerie_association.entity.Membre;
import org.example.messagerie_association.entity.Message;
import org.example.messagerie_association.entity.Role;
import org.example.messagerie_association.entity.StatutMessage;
import org.example.messagerie_association.entity.UserStatus;
import org.example.messagerie_association.repository.IMembreRepository;
import org.example.messagerie_association.repository.IMessageRepository;
import org.example.messagerie_association.repository.MembreRepository;
import org.example.messagerie_association.repository.MessageRepository;
import org.example.messagerie_association.util.AppLogger;
import org.example.messagerie_association.util.PasswordHelper;

import java.io.*;
import java.net.Socket;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class ClientHandler implements Runnable {

    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private final Socket socket;
    private final IMembreRepository membreRepository = new MembreRepository();
    private final IMessageRepository messageRepository = new MessageRepository();
    private BufferedReader in;
    private PrintWriter out;
    private Membre currentUser;

    public ClientHandler(Socket socket) {
        this.socket = socket;
    }

    @Override
    public void run() {
        try {
            in = new BufferedReader(new InputStreamReader(socket.getInputStream(), java.nio.charset.StandardCharsets.UTF_8));
            out = new PrintWriter(new OutputStreamWriter(socket.getOutputStream(), java.nio.charset.StandardCharsets.UTF_8), true);
            String line;
            while ((line = in.readLine()) != null) {
                String response = processCommand(line);
                if (response != null) {
                    out.println(response);
                }
            }
        } catch (IOException e) {
        } finally {
            if (currentUser != null) {
                try {
                    currentUser.setStatus(UserStatus.OFFLINE);
                    membreRepository.update(currentUser);
                    AppLogger.logDeconnexion(currentUser.getUsername());
                } catch (Exception ignored) {}
            }
            try {
                socket.close();
            } catch (IOException ignored) {}
        }
    }

    private void sendLine(String s) {
        out.println(s);
    }

    private String processCommand(String line) {
        if (line == null || line.isEmpty()) return Protocol.KO + Protocol.SEP + "Commande vide";
        String[] parts = line.split("\\" + Protocol.SEP, -1);
        String cmd = parts[0].trim().toUpperCase();
        try {
            switch (cmd) {
                case "LOGIN":
                    return handleLogin(parts);
                case "LOGOUT":
                    return handleLogout(parts);
                case "INSCRIRE":
                    return handleInscription(parts);
                case "GET_MEMBERS_ONLINE":
                    return handleGetMembersOnline();
                case "GET_MESSAGES":
                    return handleGetMessages();
                case "SEND_MESSAGE":
                    return handleSendMessage(parts);
                case "GET_MEMBERS":
                    return handleGetMembers();
                case "GET_ALL_MEMBERS":
                    return handleGetAllMembers(parts);
                default:
                    return Protocol.KO + Protocol.SEP + "Commande inconnue";
            }
        } catch (Exception e) {
            return Protocol.KO + Protocol.SEP + (e.getMessage() != null ? e.getMessage() : "Erreur serveur");
        }
    }

    private String handleLogin(String[] parts) {
        if (parts.length < 3) return Protocol.KO + Protocol.SEP + "LOGIN|username|password";
        String username = parts[1].trim();
        String password = parts[2].trim();
        Membre m = membreRepository.findByUsername(username);
        if (m == null || !PasswordHelper.verify(password, m.getMotDePasse())) {
            return Protocol.KO + Protocol.SEP + "Identifiants incorrects";
        }
        m.setStatus(UserStatus.OFFLINE);
        membreRepository.update(m);
        m.setStatus(UserStatus.ONLINE);
        membreRepository.update(m);
        currentUser = m;
        AppLogger.logConnexion(m.getUsername());
        return Protocol.OK + Protocol.SEP + m.getId() + Protocol.SEP + m.getUsername() + Protocol.SEP + m.getNom() + Protocol.SEP + (m.getRole() != null ? m.getRole().name() : "MEMBRE");
    }

    private String handleLogout(String[] parts) {
        if (currentUser != null) {
            currentUser.setStatus(UserStatus.OFFLINE);
            membreRepository.update(currentUser);
            AppLogger.logDeconnexion(currentUser.getUsername());
            currentUser = null;
        }
        return Protocol.OK;
    }

    private String handleInscription(String[] parts) {
        if (parts.length < 5) return Protocol.KO + Protocol.SEP + "INSCRIRE|username|nom|password|role";
        String username = parts[1].trim();
        String nom = parts[2].trim();
        String password = parts[3].trim();
        String roleStr = parts[4].trim().toUpperCase();
        if (membreRepository.findByUsername(username) != null) {
            return Protocol.KO + Protocol.SEP + "Ce nom d'utilisateur est déjà utilisé";
        }
        Role role;
        try {
            role = Role.valueOf(roleStr);
        } catch (Exception e) {
            return Protocol.KO + Protocol.SEP + "Rôle invalide (ORGANISATEUR, MEMBRE, BENEVOLE)";
        }
        Membre m = new Membre(username, nom, PasswordHelper.hash(password), role);
        membreRepository.insert(m);
        return Protocol.OK;
    }

    private String handleGetMembersOnline() {
        List<Membre> list = membreRepository.findByStatusOnline();
        sendLine(Protocol.OK);
        for (Membre m : list) {
            sendLine(Protocol.PREFIX_MEMBER + Protocol.SEP + m.getId() + Protocol.SEP + m.getUsername() + Protocol.SEP + m.getNom() + Protocol.SEP + (m.getRole() != null ? m.getRole().name() : ""));
        }
        sendLine(Protocol.END);
        return null;
    }

    private String handleGetMessages() {
        List<Message> list = messageRepository.findAll();
        sendLine(Protocol.OK);
        for (Message msg : list) {
            String exp = msg.getExpediteur() != null ? msg.getExpediteur().getNom() : "";
            String dest = msg.getDestinataire() != null ? msg.getDestinataire().getNom() : "";
            String date = msg.getDateEnvoi() != null ? msg.getDateEnvoi().format(FMT) : "";
            String statut = msg.getStatut() != null ? msg.getStatut().name() : "";
            sendLine(Protocol.PREFIX_MSG + Protocol.SEP + msg.getId() + Protocol.SEP
                    + (msg.getExpediteur() != null ? msg.getExpediteur().getId() : 0) + Protocol.SEP + exp + Protocol.SEP
                    + (msg.getDestinataire() != null ? msg.getDestinataire().getId() : 0) + Protocol.SEP + dest + Protocol.SEP
                    + (msg.getContenu() != null ? msg.getContenu().replace("|", " ") : "") + Protocol.SEP + date + Protocol.SEP + statut);
        }
        sendLine(Protocol.END);
        return null;
    }

    private String handleSendMessage(String[] parts) {
        if (currentUser == null) return Protocol.KO + Protocol.SEP + "Non authentifié";
        if (parts.length < 3) return Protocol.KO + Protocol.SEP + "SEND_MESSAGE|receiverId|contenu";
        int receiverId = Integer.parseInt(parts[1].trim());
        String contenu = parts.length > 2 ? parts[2].trim().replace("|", " ") : "";
        if (contenu.isEmpty()) return Protocol.KO + Protocol.SEP + "Contenu vide (RG7)";
        if (contenu.length() > 1000) return Protocol.KO + Protocol.SEP + "Contenu max 1000 caractères (RG7)";
        Membre dest = membreRepository.findById(receiverId);
        if (dest == null) return Protocol.KO + Protocol.SEP + "Destinataire inexistant";
        Message msg = new Message(contenu, currentUser, dest);
        messageRepository.insert(msg);
        AppLogger.logEnvoiMessage(currentUser.getUsername(), dest.getUsername(), msg.getId());
        return Protocol.OK + Protocol.SEP + msg.getId();
    }

    private String handleGetMembers() {
        if (currentUser == null) return Protocol.KO + Protocol.SEP + "Non authentifié";
        List<Membre> list = membreRepository.findAll();
        sendLine(Protocol.OK);
        for (Membre m : list) {
            sendLine(Protocol.PREFIX_MEMBER + Protocol.SEP + m.getId() + Protocol.SEP + m.getUsername() + Protocol.SEP + m.getNom() + Protocol.SEP + (m.getRole() != null ? m.getRole().name() : ""));
        }
        sendLine(Protocol.END);
        return null;
    }

    private String handleGetAllMembers(String[] parts) {
        if (currentUser == null || currentUser.getRole() != Role.ORGANISATEUR) {
            return Protocol.KO + Protocol.SEP + "Réservé aux ORGANISATEURS (RG13)";
        }
        List<Membre> list = membreRepository.findAll();
        sendLine(Protocol.OK);
        for (Membre m : list) {
            sendLine(Protocol.PREFIX_MEMBER + Protocol.SEP + m.getId() + Protocol.SEP + m.getUsername() + Protocol.SEP + m.getNom() + Protocol.SEP + (m.getRole() != null ? m.getRole().name() : "") + Protocol.SEP + (m.getStatus() != null ? m.getStatus().name() : "OFFLINE") + Protocol.SEP + (m.getDateCreation() != null ? m.getDateCreation().format(FMT) : ""));
        }
        sendLine(Protocol.END);
        return null;
    }
}
