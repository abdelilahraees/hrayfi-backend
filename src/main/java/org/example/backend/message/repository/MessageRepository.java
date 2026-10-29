package org.example.backend.message.repository;

import org.example.backend.message.entity.Message;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface MessageRepository extends JpaRepository<Message, Long> {

    /**
     * Historique bidirectionnel entre deux utilisateurs, plus récents d'abord :
     * la page 0 contient toujours les derniers messages échangés.
     */
    @EntityGraph(attributePaths = {"expediteur", "destinataire", "annonce"})
    @Query("""
           SELECT m FROM Message m
           WHERE (m.expediteur.id = :userA AND m.destinataire.id = :userB)
              OR (m.expediteur.id = :userB AND m.destinataire.id = :userA)
           ORDER BY m.timestamp DESC, m.id DESC
           """)
    Page<Message> findConversation(@Param("userA") Long userA,
                                   @Param("userB") Long userB,
                                   Pageable pageable);

    /** Id du dernier message de chaque conversation de l'utilisateur (DISTINCT ON PostgreSQL). */
    @Query(value = """
           SELECT DISTINCT ON (t.partner_id) t.id
           FROM (
               SELECT m.id, m.timestamp,
                      CASE WHEN m.expediteur_id = :userId THEN m.destinataire_id ELSE m.expediteur_id END AS partner_id
               FROM message m
               WHERE m.expediteur_id = :userId OR m.destinataire_id = :userId
           ) t
           ORDER BY t.partner_id, t.timestamp DESC, t.id DESC
           """, nativeQuery = true)
    List<Long> findLastMessageIdPerConversation(@Param("userId") Long userId);

    @EntityGraph(attributePaths = {"expediteur", "destinataire", "annonce"})
    List<Message> findByIdIn(List<Long> ids);

    /** (expediteurId, nombre) des messages non lus reçus par l'utilisateur. */
    @Query("""
           SELECT m.expediteur.id, COUNT(m)
           FROM Message m
           WHERE m.destinataire.id = :userId AND m.lu = false
           GROUP BY m.expediteur.id
           """)
    List<Object[]> countUnreadBySender(@Param("userId") Long userId);

    long countByDestinataireIdAndLuFalse(Long destinataireId);

    @Modifying
    @Query("""
           UPDATE Message m SET m.lu = true
           WHERE m.destinataire.id = :userId AND m.expediteur.id = :otherUserId AND m.lu = false
           """)
    int markConversationAsRead(@Param("userId") Long userId, @Param("otherUserId") Long otherUserId);
}
