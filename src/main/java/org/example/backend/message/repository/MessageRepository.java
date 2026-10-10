package org.example.backend.message.repository;

import org.example.backend.message.entity.Message;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface MessageRepository extends JpaRepository<Message, Long> {

    /**
     * Historique bidirectionnel entre deux utilisateurs, trié chronologiquement.
     */
    @EntityGraph(attributePaths = {"expediteur", "destinataire"})
    @Query("""
           SELECT m FROM Message m
           WHERE (m.expediteur.id = :userA AND m.destinataire.id = :userB)
              OR (m.expediteur.id = :userB AND m.destinataire.id = :userA)
           ORDER BY m.timestamp ASC
           """)
    Page<Message> findConversation(@Param("userA") Long userA,
                                   @Param("userB") Long userB,
                                   Pageable pageable);
}
