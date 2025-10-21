-- BMS Rapid Clean - Initialisation de la base de données pour Docker

-- Créer la base de données si elle n'existe pas
CREATE DATABASE IF NOT EXISTS rapidclean;

-- Utiliser la base de données
USE rapidclean;

-- Services par défaut
INSERT INTO services (name, description, price, image_url, active, created_at) VALUES
('Nettoyage Résidentiel Standard', 'Nettoyage complet de votre domicile incluant toutes les pièces, sanitaires, cuisine et sols.', 150.00, '/images/services/residentiel.jpg', true, NOW()),
('Nettoyage Commercial', 'Service de nettoyage pour bureaux, magasins et espaces commerciaux avec équipement professionnel.', 200.00, '/images/services/commercial.jpg', true, NOW()),
('Nettoyage Post-Construction', 'Nettoyage spécialisé après travaux de construction, rénovation ou déménagement.', 300.00, '/images/services/post-construction.jpg', true, NOW()),
('Nettoyage Industriel', 'Service de nettoyage pour usines, entrepôts et installations industrielles.', 400.00, '/images/services/industriel.jpg', true, NOW()),
('Nettoyage de Vitres', 'Nettoyage professionnel des vitres intérieures et extérieures avec matériel spécialisé.', 80.00, '/images/services/vitres.jpg', true, NOW()),
('Nettoyage de Tapis', 'Nettoyage en profondeur des tapis et moquettes avec aspiration et traitement.', 120.00, '/images/services/tapis.jpg', true, NOW()),
('Nettoyage Urgence', 'Service de nettoyage d\'urgence disponible 24h/24 pour situations critiques.', 250.00, '/images/services/urgence.jpg', true, NOW()),
('Nettoyage Récurrent', 'Service de nettoyage régulier (hebdomadaire, bi-hebdomadaire, mensuel) avec tarifs préférentiels.', 100.00, '/images/services/recurrent.jpg', true, NOW());

-- Utilisateur administrateur par défaut (mot de passe: admin123)
INSERT INTO users (first_name, last_name, email, password, phone, role, enabled, created_at) VALUES
('Admin', 'System', 'admin@bmsrapidclean.com', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVEFDi', '+1 (555) 000-0000', 'ADMIN', true, NOW());

-- Utilisateur client de démonstration (mot de passe: client123)
INSERT INTO users (first_name, last_name, email, password, phone, role, enabled, created_at) VALUES
('Jean', 'Dupont', 'client@example.com', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVEFDi', '+1 (555) 123-4567', 'CLIENT', true, NOW());

-- Demandes de service d'exemple
INSERT INTO service_requests (user_id, service_type, address, description, service_date, status, price, created_at, updated_at) VALUES
(2, 'Nettoyage Résidentiel Standard', '123 Rue de la Paix, Montréal, QC H1A 1A1', 'Nettoyage complet de mon appartement 3 1/2. Besoin de nettoyage en profondeur de la cuisine et des sanitaires.', '2024-01-15 10:00:00', 'PENDING', 150.00, NOW(), NOW()),
(2, 'Nettoyage de Vitres', '123 Rue de la Paix, Montréal, QC H1A 1A1', 'Nettoyage des vitres de mon balcon au 3ème étage. Attention aux précautions de sécurité.', '2024-01-16 14:00:00', 'CONFIRMED', 80.00, NOW(), NOW()),
(2, 'Nettoyage Commercial', '456 Avenue du Commerce, Montréal, QC H2B 2B2', 'Nettoyage de notre bureau de 200m². Besoin de nettoyage des espaces de travail et sanitaires.', '2024-01-17 09:00:00', 'IN_PROGRESS', 200.00, NOW(), NOW());
