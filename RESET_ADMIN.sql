-- ============================================
-- Script de Réinitialisation Complète de l'Admin
-- ============================================
-- 
-- Utilisation :
-- 1. Ouvrir votre BD (MySQL, H2, etc.)
-- 2. Exécuter ce script
-- 3. Redémarrer l'application
--
-- Email : admin@bmsrapidclean.com
-- Mot de passe : admin123
-- ============================================

-- ============================================
-- OPTION 1 : Réinitialiser BD H2 (Développement)
-- ============================================

-- Supprimer l'admin existant
DELETE FROM users WHERE email = 'admin@bmsrapidclean.com';

-- La migration sera recrééée au redémarrage par DataInitializationService

-- ============================================
-- OPTION 2 : Réinitialiser BD MySQL (Production)
-- ============================================

-- Ajouter la colonne si elle n'existe pas
-- ALTER TABLE users ADD COLUMN first_login BOOLEAN DEFAULT true;

-- Supprimer l'admin existant
-- DELETE FROM users WHERE email = 'admin@bmsrapidclean.com';

-- Insérer un nouvel admin avec BCrypt encoded password
-- Le mot de passe "admin123" encodé en BCrypt est : $2a$10$slYQmyNdGzin7olVgc9bv.WU2JVKqVbdvZEVP3NfxDBgLwODdzxJS
-- INSERT INTO users (
--   first_name,
--   last_name,
--   email,
--   password,
--   phone,
--   role,
--   enabled,
--   first_login,
--   created_at
-- ) VALUES (
--   'Admin',
--   'System',
--   'admin@bmsrapidclean.com',
--   '$2a$10$slYQmyNdGzin7olVgc9bv.WU2JVKqVbdvZEVP3NfxDBgLwODdzxJS',
--   '+1 (555) 000-0000',
--   'ADMIN',
--   true,
--   false,
--   NOW()
-- );

-- ============================================
-- Vérification après exécution
-- ============================================

-- Vérifier que l'admin existe
SELECT * FROM users WHERE email = 'admin@bmsrapidclean.com';

-- Vérifier tous les utilisateurs
SELECT id, email, first_name, last_name, role, enabled, first_login FROM users;
