-- =============================================================
--  V5 : négociation client ⇄ artisan
--   - un message peut porter sur une annonce et contenir une offre de prix
--   - suivi lu / non lu des messages
--   - une demande peut référencer l'annonce d'origine et le prix négocié
-- =============================================================

ALTER TABLE message
    ADD COLUMN annonce_id   BIGINT,
    ADD COLUMN prix_propose NUMERIC(10,2),
    ADD COLUMN lu           BOOLEAN NOT NULL DEFAULT FALSE,
    ADD CONSTRAINT fk_message_annonce FOREIGN KEY (annonce_id) REFERENCES annonce(id) ON DELETE SET NULL;

-- Accélère le compteur "messages non lus" par conversation
CREATE INDEX idx_message_non_lus ON message(destinataire_id, lu);

ALTER TABLE demande_service
    ADD COLUMN annonce_id   BIGINT,
    ADD COLUMN prix_convenu NUMERIC(10,2),
    ADD CONSTRAINT fk_demande_annonce FOREIGN KEY (annonce_id) REFERENCES annonce(id) ON DELETE SET NULL;
