-- =============================================================
--  V6 : rôle administrateur et validation des comptes artisans
-- =============================================================

-- ---------- Administrateur (sous-type JOINED de app_user) ----------
CREATE TABLE admin (
    id  BIGINT  PRIMARY KEY,
    CONSTRAINT fk_admin_app_user FOREIGN KEY (id) REFERENCES app_user(id) ON DELETE CASCADE
);

-- ---------- Validation des artisans ----------
-- Les artisans déjà inscrits sont considérés comme validés ; les nouveaux attendent un administrateur.
ALTER TABLE artisan ADD COLUMN statut_validation VARCHAR(20) NOT NULL DEFAULT 'VALIDE';
ALTER TABLE artisan ALTER COLUMN statut_validation SET DEFAULT 'EN_ATTENTE';
ALTER TABLE artisan ADD COLUMN motif_refus  TEXT;
ALTER TABLE artisan ADD COLUMN valide_le    TIMESTAMP;

CREATE INDEX idx_artisan_statut_validation ON artisan(statut_validation);
