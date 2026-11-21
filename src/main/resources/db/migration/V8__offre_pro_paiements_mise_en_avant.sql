-- =============================================================
--  V8 : téléphone, offre Pro (suivi manuel des paiements), mise en avant des annonces
--  Pas de paiement en ligne : l'équipe encaisse (CashPlus, Wafacash, virement, espèces)
--  puis enregistre le paiement, ce qui prolonge l'offre Pro ou met une annonce en avant.
-- =============================================================

ALTER TABLE app_user ADD COLUMN telephone VARCHAR(20);

-- Offre Pro : échéance, et offerte (période de lancement) ou payée
ALTER TABLE artisan ADD COLUMN pro_jusqu_au  TIMESTAMP;
ALTER TABLE artisan ADD COLUMN pro_offert    BOOLEAN NOT NULL DEFAULT TRUE;
ALTER TABLE artisan ADD COLUMN note_admin    TEXT;
-- Les artisans déjà validés reçoivent la période offerte de 90 jours
UPDATE artisan SET pro_jusqu_au = CURRENT_TIMESTAMP + INTERVAL '90 days' WHERE statut_validation = 'VALIDE';

-- Mise en avant d'une annonce jusqu'à une date
ALTER TABLE annonce ADD COLUMN sponsorise_jusqu_au TIMESTAMP;
CREATE INDEX idx_annonce_sponsorise ON annonce(sponsorise_jusqu_au);

-- ---------- Paiements encaissés hors ligne ----------
CREATE TABLE paiement (
    id              BIGSERIAL       PRIMARY KEY,
    artisan_id      BIGINT          NOT NULL,
    objet           VARCHAR(30)     NOT NULL,   -- ABONNEMENT_PRO | MISE_EN_AVANT
    montant         NUMERIC(10,2)   NOT NULL CHECK (montant >= 0),
    moyen           VARCHAR(20)     NOT NULL,   -- CASHPLUS | WAFACASH | VIREMENT | ESPECES | AUTRE
    reference       VARCHAR(100),
    duree_jours     INTEGER         NOT NULL CHECK (duree_jours > 0),
    annonce_id      BIGINT,
    note            TEXT,
    enregistre_par  BIGINT,
    created_at      TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_paiement_artisan  FOREIGN KEY (artisan_id)     REFERENCES artisan(id)  ON DELETE CASCADE,
    CONSTRAINT fk_paiement_annonce  FOREIGN KEY (annonce_id)     REFERENCES annonce(id)  ON DELETE SET NULL,
    CONSTRAINT fk_paiement_admin    FOREIGN KEY (enregistre_par) REFERENCES app_user(id) ON DELETE SET NULL
);

CREATE INDEX idx_paiement_artisan ON paiement(artisan_id, created_at DESC);
CREATE INDEX idx_paiement_date    ON paiement(created_at);
