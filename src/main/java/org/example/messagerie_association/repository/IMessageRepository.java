package org.example.messagerie_association.repository;

import org.example.messagerie_association.entity.Message;

import java.util.List;

public interface IMessageRepository {

    void insert(Message message);

    Message findById(int id);

    List<Message> findAll();

    List<Message> findByExpediteurId(int expediteurId);

    List<Message> findByDestinataireId(int destinataireId);

    void update(Message message);

    void delete(int id);
}
