-- =============================================================
--  V3 : annonces de services publiées par les artisans
--  (ex : "Réparation de fuite et débouchage" avec photos)
-- =============================================================

CREATE TABLE annonce (
    id            BIGSERIAL      PRIMARY KEY,
    artisan_id    BIGINT         NOT NULL,
    titre         VARCHAR(150)   NOT NULL,
    description   TEXT           NOT NULL,
    metier        VARCHAR(100)   NOT NULL,
    ville         VARCHAR(100),
    prix_min      NUMERIC(10,2),
    prix_max      NUMERIC(10,2),
    active        BOOLEAN        NOT NULL DEFAULT TRUE,
    -- dénormalisés : permettent de filtrer / trier par note sans agrégat à chaque recherche
    note_moyenne  NUMERIC(3,2)   NOT NULL DEFAULT 0,
    nb_avis       INTEGER        NOT NULL DEFAULT 0,
    created_at    TIMESTAMP      NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at    TIMESTAMP      NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_annonce_artisan FOREIGN KEY (artisan_id) REFERENCES artisan(id) ON DELETE CASCADE,
    CONSTRAINT ck_annonce_prix CHECK (prix_min IS NULL OR prix_max IS NULL OR prix_min <= prix_max)
);

CREATE INDEX idx_annonce_artisan ON annonce(artisan_id);
CREATE INDEX idx_annonce_metier  ON annonce(metier);
CREATE INDEX idx_annonce_ville   ON annonce(ville);
CREATE INDEX idx_annonce_note    ON annonce(note_moyenne DESC);

CREATE TABLE annonce_image (
    id          BIGSERIAL      PRIMARY KEY,
    annonce_id  BIGINT         NOT NULL,
    url         VARCHAR(255)   NOT NULL,
    position    INTEGER        NOT NULL DEFAULT 0,
    CONSTRAINT fk_annonce_image_annonce FOREIGN KEY (annonce_id) REFERENCES annonce(id) ON DELETE CASCADE
);

CREATE INDEX idx_annonce_image_annonce ON annonce_image(annonce_id, position);
