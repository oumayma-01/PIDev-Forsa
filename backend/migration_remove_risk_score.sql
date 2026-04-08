-- Migration : Suppression de la colonne risk_score de la table credit_request
-- Date : 2026-03-01
-- Description : Le champ riskScore n'est plus nécessaire, on garde uniquement isRisky, riskLevel et scoredAt

-- Vérifier que la colonne existe avant de la supprimer
ALTER TABLE credit_request DROP COLUMN IF EXISTS risk_score;

-- Vérification : Afficher la structure de la table
-- DESCRIBE credit_request;

