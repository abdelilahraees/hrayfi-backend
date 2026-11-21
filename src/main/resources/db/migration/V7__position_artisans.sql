-- =============================================================
--  V7 : position des artisans pour la recherche « autour de moi »
--  position_approx = true : centre de la ville déclarée, pas une position choisie par l'artisan
-- =============================================================
ALTER TABLE artisan ADD COLUMN latitude         DOUBLE PRECISION;
ALTER TABLE artisan ADD COLUMN longitude        DOUBLE PRECISION;
ALTER TABLE artisan ADD COLUMN position_approx  BOOLEAN NOT NULL DEFAULT TRUE;

CREATE INDEX idx_artisan_position ON artisan(latitude, longitude);
