package org.example.messagerie_association.repository;

import org.example.messagerie_association.entity.Membre;

import java.util.List;

public interface IMembreRepository {

    void insert(Membre membre);

    Membre findById(int id);

    Membre findByUsername(String username);

    List<Membre> findAll();

    List<Membre> findByStatusOnline();

    void update(Membre membre);

    void delete(int id);
}
