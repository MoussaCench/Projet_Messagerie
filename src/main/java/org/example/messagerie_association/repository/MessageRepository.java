package org.example.messagerie_association.repository;

import org.example.messagerie_association.database.DatabaseManager;
import org.example.messagerie_association.entity.Message;

import javax.persistence.EntityManager;
import javax.persistence.EntityTransaction;
import java.util.List;

public class MessageRepository implements IMessageRepository {

    private final EntityManager em = DatabaseManager.createEntityManager();

    @Override
    public void insert(Message message) {
        EntityTransaction tx = em.getTransaction();
        try {
            tx.begin();
            em.persist(message);
            tx.commit();
        } catch (Exception e) {
            if (tx.isActive()) tx.rollback();
            throw e;
        }
    }

    @Override
    public Message findById(int id) {
        return em.find(Message.class, id);
    }

    @Override
    public List<Message> findAll() {
        return em.createQuery("FROM Message m ORDER BY m.dateEnvoi ASC", Message.class).getResultList();
    }

    @Override
    public List<Message> findByExpediteurId(int expediteurId) {
        return em.createQuery("FROM Message m WHERE m.expediteur.id = :id ORDER BY m.dateEnvoi ASC", Message.class)
                .setParameter("id", expediteurId)
                .getResultList();
    }

    @Override
    public List<Message> findByDestinataireId(int destinataireId) {
        return em.createQuery("FROM Message m WHERE m.destinataire.id = :id ORDER BY m.dateEnvoi ASC", Message.class)
                .setParameter("id", destinataireId)
                .getResultList();
    }

    @Override
    public void update(Message message) {
        EntityTransaction tx = em.getTransaction();
        try {
            tx.begin();
            em.merge(message);
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
            Message m = em.find(Message.class, id);
            if (m != null) em.remove(m);
            tx.commit();
        } catch (Exception e) {
            if (tx.isActive()) tx.rollback();
            throw e;
        }
    }
}
