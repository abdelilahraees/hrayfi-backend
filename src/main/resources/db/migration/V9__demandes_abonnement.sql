-- =============================================================
--  V9 : demandes de changement d'offre envoyées par les artisans depuis l'application
--  L'équipe décide : contacter, encaisser (le paiement est alors enregistré), refuser ou suspendre le compte.
-- =============================================================
CREATE TABLE demande_abonnement (
    id              BIGSERIAL       PRIMARY KEY,
    artisan_id      BIGINT          NOT NULL,
    formule         VARCHAR(30)     NOT NULL,   -- PRO_MOIS | PRO_TRIMESTRE | PRO_AN | MISE_EN_AVANT_14 | MISE_EN_AVANT_30
    annonce_id      BIGINT,                     -- annonce à mettre en avant
    moyen_souhaite  VARCHAR(20)     NOT NULL,
    message         TEXT,
    statut          VARCHAR(20)     NOT NULL DEFAULT 'EN_ATTENTE',
    reponse_admin   TEXT,
    paiement_id     BIGINT,
    traitee_par     BIGINT,
    traitee_le      TIMESTAMP,
    created_at      TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_dab_artisan   FOREIGN KEY (artisan_id)  REFERENCES artisan(id)  ON DELETE CASCADE,
    CONSTRAINT fk_dab_annonce   FOREIGN KEY (annonce_id)  REFERENCES annonce(id)  ON DELETE SET NULL,
    CONSTRAINT fk_dab_paiement  FOREIGN KEY (paiement_id) REFERENCES paiement(id) ON DELETE SET NULL,
    CONSTRAINT fk_dab_admin     FOREIGN KEY (traitee_par) REFERENCES app_user(id) ON DELETE SET NULL
);

CREATE INDEX idx_dab_statut  ON demande_abonnement(statut, created_at);
CREATE INDEX idx_dab_artisan ON demande_abonnement(artisan_id, created_at DESC);
