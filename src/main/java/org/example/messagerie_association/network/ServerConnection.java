package org.example.messagerie_association.network;

import javafx.application.Platform;
import org.example.messagerie_association.server.Protocol;

import java.io.*;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.locks.ReentrantLock;

public class ServerConnection {

    private static final String DEFAULT_HOST = "localhost";
    private static final int DEFAULT_PORT = 9000;

    private Socket socket;
    private BufferedReader in;
    private PrintWriter out;
    private final ReentrantLock lock = new ReentrantLock();
    private Runnable onDisconnect;

    public void setOnDisconnect(Runnable runnable) {
        this.onDisconnect = runnable;
    }

    public boolean connect() {
        return connect(DEFAULT_HOST, DEFAULT_PORT);
    }

    public boolean connect(String host, int port) {
        lock.lock();
        try {
            if (socket != null && socket.isConnected()) return true;
            socket = new Socket(host, port);
            in = new BufferedReader(new InputStreamReader(socket.getInputStream(), StandardCharsets.UTF_8));
            out = new PrintWriter(new OutputStreamWriter(socket.getOutputStream(), StandardCharsets.UTF_8), true);
            return true;
        } catch (IOException e) {
            return false;
        } finally {
            lock.unlock();
        }
    }

    public void disconnect() {
        lock.lock();
        try {
            if (socket != null) {
                try {
                    socket.close();
                } catch (IOException ignored) {}
                socket = null;
                in = null;
                out = null;
            }
        } finally {
            lock.unlock();
        }
    }

    public boolean isConnected() {
        return socket != null && socket.isConnected() && !socket.isClosed();
    }

    private void fireDisconnect() {
        disconnect();
        if (onDisconnect != null) {
            Platform.runLater(onDisconnect);
        }
    }

    public String sendAndWait(String line) throws IOException {
        lock.lock();
        try {
            if (out == null || in == null) throw new IOException("Non connecté");
            out.println(line);
            String response = in.readLine();
            if (response == null) throw new IOException("Connexion fermée par le serveur");
            return response;
        } catch (IOException e) {
            fireDisconnect();
            throw e;
        } finally {
            lock.unlock();
        }
    }

    public List<String> sendAndReadBlock(String line) throws IOException {
        lock.lock();
        try {
            if (out == null || in == null) throw new IOException("Non connecté");
            out.println(line);
            String first = in.readLine();
            if (first == null) throw new IOException("Connexion fermée par le serveur");
            if (first.startsWith(Protocol.KO + Protocol.SEP)) {
                throw new IOException(first.substring(Protocol.KO.length() + 1).trim());
            }
            List<String> lines = new ArrayList<>();
            String l;
            while ((l = in.readLine()) != null && !Protocol.END.equals(l.trim())) {
                lines.add(l);
            }
            if (l == null) throw new IOException("Connexion fermée");
            return lines;
        } catch (IOException e) {
            fireDisconnect();
            throw e;
        } finally {
            lock.unlock();
        }
    }

    public static String getHost() {
        return DEFAULT_HOST;
    }

    public static int getPort() {
        return DEFAULT_PORT;
    }
}
