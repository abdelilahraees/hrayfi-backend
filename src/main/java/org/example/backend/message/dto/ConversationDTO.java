package org.example.backend.message.dto;

import org.example.backend.user.entity.Role;

import java.math.BigDecimal;
import java.time.Instant;

/**
 * Une ligne de la boîte de réception : l'interlocuteur et le dernier message échangé.
 */
public record ConversationDTO(
        Long interlocuteurId,
        String interlocuteurNomComplet,
        Role interlocuteurRole,
        String interlocuteurMetier,     // null si l'interlocuteur est un client
        String dernierMessage,
        Long dernierExpediteurId,
        BigDecimal dernierPrixPropose,
        Instant dernierTimestamp,
        long nonLus
) {}
