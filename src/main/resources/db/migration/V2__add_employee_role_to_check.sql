-- Migration : Ajouter le rôle EMPLOYEE à la contrainte CHECK
-- Cette migration ajoute EMPLOYEE aux rôles autorisés dans la table users

-- Supprimer la contrainte CHECK existante
ALTER TABLE users DROP CONSTRAINT IF EXISTS users_role_check;

-- Ajouter la nouvelle contrainte CHECK avec les rôles ADMIN, EMPLOYEE, et CLIENT
ALTER TABLE users ADD CONSTRAINT users_role_check CHECK (role IN ('ADMIN', 'EMPLOYEE', 'CLIENT'));
