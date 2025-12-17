-- Migration : Ajouter la colonne firstLogin à la table users
-- Cette migration ajoute la propriété firstLogin pour tracker les premières connexions

-- Ajoute la colonne de façon sûre si elle n'existe pas, puis garantit des valeurs non NULL
ALTER TABLE IF NOT EXISTS users ADD COLUMN IF NOT EXISTS first_login BOOLEAN;

-- Pour les lignes existantes, remplir les valeurs NULL par 'false'
UPDATE users SET first_login = false WHERE first_login IS NULL;

-- Définir la valeur par défaut et rendre la colonne NOT NULL
ALTER TABLE users ALTER COLUMN first_login SET DEFAULT false;
ALTER TABLE users ALTER COLUMN first_login SET NOT NULL;
