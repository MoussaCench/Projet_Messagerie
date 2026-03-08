package org.example.messagerie_association.database;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;

public final class DatabaseManager {

    private static final String PERSISTENCE_UNIT = "PERSISTENCE";
    private static EntityManagerFactory emf;

    private DatabaseManager() {
    }

    public static EntityManagerFactory getEntityManagerFactory() {
        if (emf == null) {
            emf = Persistence.createEntityManagerFactory(PERSISTENCE_UNIT);
        }
        return emf;
    }

    public static EntityManager createEntityManager() {
        return getEntityManagerFactory().createEntityManager();
    }

    public static boolean testConnection() {
        try {
            getEntityManagerFactory().createEntityManager().close();
            return true;
        } catch (Exception e) {
            return false;
        }
    }
}
