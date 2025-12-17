-- Script SQL pour insérer les services de nettoyage Fast-Food
-- BMS RapidClean - Services extraits des images

-- Supprimer les anciens services si nécessaire (optionnel)
-- DELETE FROM services WHERE name LIKE '%Fast-Food%' OR name LIKE '%fast-food%';

-- Insérer les nouveaux services
INSERT INTO services (name, description, price, image_url, active, created_at) VALUES

-- 1. Nettoyage Professionnel Fast-Food
('Nettoyage Professionnel Fast-Food', 
'Services spécialisés pour chaînes de restauration rapide, incluant cuisines, salles de restauration, drive-thru, zones clients et extérieurs. Respect des normes d''hygiène HACCP pour garantir un environnement propre et sécurisé.',
450.00,
'/images/services/fast-food.jpg',
true,
NOW()),

-- 2. Nettoyage des Salles
('Nettoyage des Salles', 
'Balayage, brossage, raclage, serpillage et nettoyage complet des tables, chaises, vitres, murs et portes. Maintien d''une salle de restauration impeccable pour offrir une expérience client optimale.',
180.00,
'/images/services/salles.jpg',
true,
NOW()),

-- 3. Entretien des Sanitaires
('Entretien des Sanitaires', 
'Lavage complet des sanitaires, miroirs, robinets, carrelages et sols. Détartrage régulier pour une hygiène parfaite. Utilisation de produits désinfectants professionnels conformes aux normes sanitaires.',
120.00,
'/images/services/sanitaires.jpg',
true,
NOW()),

-- 4. Nettoyage des Vitres
('Nettoyage des Vitres', 
'Nettoyage intérieur et extérieur des vitres, portes vitrées et encadrements pour une transparence sans traces. Utilisation de matériel professionnel et produits adaptés pour un résultat impeccable.',
150.00,
'/images/services/vitres.jpg',
true,
NOW()),

-- 5. Nettoyage de Cuisine
('Nettoyage de Cuisine', 
'Dégraissage complet des hottes, grills, friteuses, plans de travail, inox et sols. Désinfection et nettoyage de tout le matériel alimentaire. Respect strict des normes HACCP pour la sécurité alimentaire.',
350.00,
'/images/services/cuisine.jpg',
true,
NOW()),

-- 6. Caisse & Drive
('Caisse & Drive', 
'Nettoyage des comptoirs, bornes de commande, guichets drive et zones clients. Entretien complet des surfaces en contact avec le public pour garantir un environnement propre et accueillant.',
100.00,
'/images/services/caisse-drive.jpg',
true,
NOW()),

-- 7. Locaux Techniques & Arrières
('Locaux Techniques & Arrières', 
'Nettoyage des couloirs, zones techniques, plonge, local boissons, ventilation et poubelles. Maintien d''un espace de travail sain et ordonné pour le personnel en arrière-cuisine.',
200.00,
'/images/services/locaux-techniques.jpg',
true,
NOW()),

-- 8. Vestiaires & Douches
('Vestiaires & Douches', 
'Nettoyage complet des vestiaires, casiers, douches et sanitaires du personnel pour un environnement propre et confortable. Désinfection régulière pour préserver l''hygiène des espaces de repos.',
130.00,
'/images/services/vestiaires.jpg',
true,
NOW()),

-- 9. Nettoyage Extérieur
('Nettoyage Extérieur', 
'Ramassage des déchets, lavage des terrasses, entretien des jardinières et cabines drive. Nettoyage haute pression mensuel des sols et façades pour maintenir une image de marque impeccable.',
250.00,
'/images/services/exterieur.jpg',
true,
NOW());

-- Vérification des insertions
SELECT id, name, price, active, created_at FROM services ORDER BY created_at DESC LIMIT 10;



