package org.example.messagerie_association.repository;

import org.example.messagerie_association.database.DatabaseManager;
import org.example.messagerie_association.entity.Membre;

import javax.persistence.EntityManager;
import javax.persistence.EntityTransaction;
import java.util.List;

public class MembreRepository implements IMembreRepository {

    private final EntityManager em = DatabaseManager.createEntityManager();

    @Override
    public void insert(Membre membre) {
        EntityTransaction tx = em.getTransaction();
        try {
            tx.begin();
            em.persist(membre);
            tx.commit();
        } catch (Exception e) {
            if (tx.isActive()) tx.rollback();
            throw e;
        }
    }

    @Override
    public Membre findById(int id) {
        return em.find(Membre.class, id);
    }

    @Override
    public Membre findByUsername(String username) {
        var list = em.createQuery("FROM Membre m WHERE m.username = :username", Membre.class)
                .setParameter("username", username)
                .getResultList();
        return list.isEmpty() ? null : list.get(0);
    }

    @Override
    public List<Membre> findAll() {
        return em.createQuery("FROM Membre m ORDER BY m.nom", Membre.class).getResultList();
    }

    @Override
    public List<Membre> findByStatusOnline() {
        return em.createQuery("FROM Membre m WHERE m.status = org.example.messagerie_association.entity.UserStatus.ONLINE ORDER BY m.nom", Membre.class).getResultList();
    }

    @Override
    public void update(Membre membre) {
        EntityTransaction tx = em.getTransaction();
        try {
            tx.begin();
            em.merge(membre);
            tx.commit();
        } catch (Exception e) {
            if (tx.isActive()) tx.rollback();
            throw e;
        }
    }

    @Override
    public void delete(int id) {
        EntityTransaction tx = em.getTransaction();
        try {
            tx.begin();
            Membre m = em.find(Membre.class, id);
            if (m != null) em.remove(m);
            tx.commit();
        } catch (Exception e) {
            if (tx.isActive()) tx.rollback();
            throw e;
        }
    }
}
