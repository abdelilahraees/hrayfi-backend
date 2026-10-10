-- =============================================================
--  V2 : notation + commentaires + notifications
-- =============================================================

-- ---------- Évaluations (1 par demande au maximum) ----------
CREATE TABLE evaluation (
    id          BIGSERIAL     PRIMARY KEY,
    demande_id  BIGINT        NOT NULL,
    client_id   BIGINT        NOT NULL,  -- dénormalisé : évite un JOIN quand on liste les évals d'un client
    artisan_id  BIGINT        NOT NULL,  -- dénormalisé : essentiel pour les requêtes dashboard (AVG par artisan)
    note        INTEGER       NOT NULL CHECK (note BETWEEN 1 AND 5),
    commentaire TEXT,
    created_at  TIMESTAMP     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uk_evaluation_demande UNIQUE (demande_id),
    CONSTRAINT fk_evaluation_demande FOREIGN KEY (demande_id) REFERENCES demande_service(id) ON DELETE CASCADE,
    CONSTRAINT fk_evaluation_client  FOREIGN KEY (client_id)  REFERENCES client(id),
    CONSTRAINT fk_evaluation_artisan FOREIGN KEY (artisan_id) REFERENCES artisan(id)
);

CREATE INDEX idx_evaluation_artisan ON evaluation(artisan_id);
CREATE INDEX idx_evaluation_client  ON evaluation(client_id);

-- ---------- Notifications ----------
CREATE TABLE notification (
    id              BIGSERIAL     PRIMARY KEY,
    destinataire_id BIGINT        NOT NULL,
    type            VARCHAR(40)   NOT NULL,
    titre           VARCHAR(150)  NOT NULL,
    message         VARCHAR(500)  NOT NULL,
    lu              BOOLEAN       NOT NULL DEFAULT FALSE,
    lien_cible      VARCHAR(255),           -- URL/route cliquable côté front, ex: /demandes/42
    created_at      TIMESTAMP     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_notification_destinataire FOREIGN KEY (destinataire_id) REFERENCES app_user(id) ON DELETE CASCADE
);

-- Index composite : accélère la requête "mes notifications non lues triées"
CREATE INDEX idx_notification_destinataire_lu ON notification(destinataire_id, lu, created_at DESC);