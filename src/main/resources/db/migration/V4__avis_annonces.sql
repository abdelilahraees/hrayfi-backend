-- =============================================================
--  V4 : avis des clients sur les annonces (note 1-5 + commentaire)
--  Un client laisse au plus un avis par annonce (il peut le modifier).
-- =============================================================

CREATE TABLE avis_annonce (
    id           BIGSERIAL     PRIMARY KEY,
    annonce_id   BIGINT        NOT NULL,
    client_id    BIGINT        NOT NULL,
    note         INTEGER       NOT NULL CHECK (note BETWEEN 1 AND 5),
    commentaire  TEXT,
    created_at   TIMESTAMP     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at   TIMESTAMP     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uk_avis_annonce_client UNIQUE (annonce_id, client_id),
    CONSTRAINT fk_avis_annonce FOREIGN KEY (annonce_id) REFERENCES annonce(id) ON DELETE CASCADE,
    CONSTRAINT fk_avis_client  FOREIGN KEY (client_id)  REFERENCES client(id)  ON DELETE CASCADE
);

CREATE INDEX idx_avis_annonce ON avis_annonce(annonce_id, created_at DESC);
CREATE INDEX idx_avis_client  ON avis_annonce(client_id);
