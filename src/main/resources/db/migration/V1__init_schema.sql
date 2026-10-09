-- =============================================================
--  V1 : schéma initial - utilisateurs, demandes, messages
--  Stratégie d'héritage JPA : JOINED
--  → une table 'app_user' partagée + une table par sous-type
-- =============================================================

-- ---------- Table parente ----------
CREATE TABLE app_user (
    id          BIGSERIAL       PRIMARY KEY,
    email       VARCHAR(150)    NOT NULL,
    password    VARCHAR(255)    NOT NULL,
    nom         VARCHAR(80)     NOT NULL,
    prenom      VARCHAR(80)     NOT NULL,
    role        VARCHAR(20)     NOT NULL,
    created_at  TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uk_app_user_email UNIQUE (email)
);

CREATE INDEX idx_app_user_role ON app_user(role);

-- ---------- Client ----------
CREATE TABLE client (
    id            BIGINT         PRIMARY KEY,
    adresse       VARCHAR(255),
    preferences   TEXT,
    CONSTRAINT fk_client_app_user FOREIGN KEY (id) REFERENCES app_user(id) ON DELETE CASCADE
);

-- ---------- Artisan ----------
CREATE TABLE artisan (
    id             BIGINT         PRIMARY KEY,
    metier         VARCHAR(100)   NOT NULL,
    tarif_horaire  NUMERIC(10,2),
    description    TEXT,
    ville          VARCHAR(100),
    disponibilite  VARCHAR(20)    NOT NULL DEFAULT 'DISPONIBLE',
    CONSTRAINT fk_artisan_app_user FOREIGN KEY (id) REFERENCES app_user(id) ON DELETE CASCADE
);

CREATE INDEX idx_artisan_metier ON artisan(metier);
CREATE INDEX idx_artisan_ville  ON artisan(ville);

-- ---------- Demande de service ----------
CREATE TABLE demande_service (
    id                BIGSERIAL       PRIMARY KEY,
    client_id         BIGINT          NOT NULL,
    artisan_id        BIGINT          NOT NULL,
    description       TEXT            NOT NULL,
    date_intervention TIMESTAMP       NOT NULL,
    statut            VARCHAR(20)     NOT NULL,
    prix_final        NUMERIC(10,2),
    created_at        TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at        TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_demande_client  FOREIGN KEY (client_id)  REFERENCES client(id),
    CONSTRAINT fk_demande_artisan FOREIGN KEY (artisan_id) REFERENCES artisan(id)
);

CREATE INDEX idx_demande_client  ON demande_service(client_id);
CREATE INDEX idx_demande_artisan ON demande_service(artisan_id);
CREATE INDEX idx_demande_statut  ON demande_service(statut);

-- ---------- Messages (chat) ----------
CREATE TABLE message (
    id              BIGSERIAL      PRIMARY KEY,
    expediteur_id   BIGINT         NOT NULL,
    destinataire_id BIGINT         NOT NULL,
    contenu         TEXT           NOT NULL,
    timestamp       TIMESTAMP      NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_message_expediteur   FOREIGN KEY (expediteur_id)   REFERENCES app_user(id),
    CONSTRAINT fk_message_destinataire FOREIGN KEY (destinataire_id) REFERENCES app_user(id)
);

CREATE INDEX idx_message_expediteur   ON message(expediteur_id);
CREATE INDEX idx_message_destinataire ON message(destinataire_id);
CREATE INDEX idx_message_conversation ON message(expediteur_id, destinataire_id, timestamp);
