package org.example.messagerie_association.session;

import org.example.messagerie_association.entity.Membre;

public final class Session {

    private static Membre currentUser;

    private Session() {
    }

    public static Membre getCurrentUser() {
        return currentUser;
    }

    public static void setCurrentUser(Membre membre) {
        currentUser = membre;
    }

    public static void clear() {
        currentUser = null;
    }

    public static boolean isLoggedIn() {
        return currentUser != null;
    }
}
