# 🧑‍💼 Système de Gestion des Employés - BMS0 Clean Solutions

## 📌 Vue d'Ensemble

Le système de gestion des employés est un module complet qui permet aux administrateurs de gérer les employés de l'entreprise et aux employés de suivre leurs activités quotidiennes, leurs absences et de signaler des problèmes au travail.

### 🎯 Objectifs Principaux
- Création et gestion des comptes employés par l'administrateur
- Suivi des heures de travail (pointage)
- Gestion des absences
- Signalement des observations/problèmes au lieu de travail
- Tableau de bord centralisé pour l'administration

---

## 👥 Rôles et Responsabilités

### 🔐 Administrateur
L'administrateur a accès à toutes les fonctionnalités de gestion :
- ✅ Créer, modifier et supprimer les employés
- ✅ Réinitialiser les mots de passe des employés
- ✅ Consulter les profils détaillés des employés
- ✅ Suivre les pointages de chaque employé
- ✅ Consulter les déclarations d'absence
- ✅ Gérer les observations/signalements
- ✅ Répondre aux signalements des employés

### 👨‍🔧 Employé
L'employé a accès aux fonctionnalités suivantes :
- ✅ Enregistrer son arrivée et départ
- ✅ Consulter son historique de pointage
- ✅ Déclarer une absence
- ✅ Signaler des problèmes au travail
- ✅ Consulter l'historique de ses signalements
- ✅ Modifier son profil
- ✅ **Obligation de changer son mot de passe à la première connexion**

---

## 🔄 Flux de Travail Complet

### 1️⃣ Création d'un Compte Employé

#### Étape 1 : L'Administrateur Crée le Compte
```
Admin accède à : /admin/employees/new
↓
Remplit le formulaire :
  - Prénom
  - Nom
  - Email
  - Téléphone
↓
Système génère automatiquement :
  - Mot de passe par défaut : "temp123"
  - Role : EMPLOYEE
  - Statut : Actif
  - firstLogin : true (marque comme première connexion)
↓
Message : "Employé créé avec succès. Email: xxx@xxx.com / Mot de passe: temp123"
```

#### Étape 2 : Communication des Identifiants
L'administrateur transmet à l'employé :
```
Email : [email de l'employé]
Mot de passe initial : temp123
URL de connexion : [adresse de l'application]
```

### 2️⃣ Première Connexion de l'Employé

#### Étape 1 : Connexion Initiale
```
L'employé accède à la page de connexion
Email : email@example.com
Mot de passe : temp123
↓
Connexion réussie
```

#### Étape 2 : Redirection Obligatoire
```
Le système détecte firstLogin = true
↓
Redirection automatique vers : /employee/change-password
↓
Message : "C'est votre première connexion. 
           Veuillez modifier votre mot de passe pour continuer."
```

#### Étape 3 : Modification du Mot de Passe
```
Formulaire obligatoire :
  - Nouveau mot de passe (min. 6 caractères)
  - Confirmation du mot de passe

L'employé remplit et valide
↓
Système :
  - Encode le nouveau mot de passe (BCrypt)
  - Marque firstLogin = false
  - Sauvegarde en base de données
↓
Message : "Mot de passe modifié avec succès. 
           Veuillez vous reconnecter."
↓
Déconnexion automatique
Redirection vers : /employee-login?logout=true
```

#### Étape 4 : Reconnexion
```
L'employé se reconnecte avec :
  - Email : email@example.com
  - Mot de passe : [son nouveau mot de passe]
↓
Accès au tableau de bord : /employee/dashboard
```

---

## 📊 Fonctionnalités Détaillées

### A. 📈 Tableau de Bord Employé (`/employee/dashboard`)

**Affiche :**
- Vue d'ensemble de l'activité du jour
- Pointage du jour (heure d'arrivée/départ si enregistrée)
- Pointages du mois en cours
- Absences du mois en cours
- Observations récentes
- Boutons d'accès rapide aux différentes sections

**Navigation :**
```
Dashboard → Pointage | Absences | Observations | Profil | Déconnexion
```

### B. ⏰ Pointage des Heures (`/employee/time-tracking`)

**Fonctionnalités :**

1. **Enregistrement de l'Arrivée**
   ```
   Bouton : "📍 Enregistrer Arrivée"
   ↓
   Système :
     - Récupère la date du jour
     - Enregistre l'heure actuelle (LocalTime.now())
     - Crée ou met à jour TimeTracking
   ↓
   Message : "Heure d'arrivée enregistrée à HH:mm"
   ```

2. **Enregistrement du Départ**
   ```
   Bouton : "📍 Enregistrer Départ"
   ↓
   Vérification :
     - Confirmation qu'une arrivée a été enregistrée
     - Sinon : message d'erreur
   ↓
   Système :
     - Enregistre l'heure de départ
   ↓
   Message : "Heure de départ enregistrée à HH:mm"
   ```

3. **Historique des Pointages**
   ```
   Tableau affichant les 30 derniers jours :
   - Date
   - Jour de la semaine
   - Heure d'arrivée
   - Heure de départ
   - Durée travaillée
   - Notes (optionnel)
   ```

**Modèle de Données : TimeTracking**
```java
- id : Long (PK)
- user : User (FK)
- trackingDate : LocalDate
- arrivalTime : LocalTime
- departureTime : LocalTime
- notes : String
```

### C. 📅 Gestion des Absences (`/employee/absences`)

**Déclaration d'Absence :**
```
Formulaire :
  - Date de l'absence (DatePicker)
  - Type d'absence (Enum)
  - Motif/Justification (TextArea)
  
Types d'absence disponibles :
  1. Congé Maladie
  2. Congé Payé
  3. Congé Non Payé
  4. Jour Férié
  5. Congé Spécial
↓
Système :
  - Crée une nouvelle absence
  - Enregistre la date/heure de création
↓
Message : "Absence déclarée avec succès"
```

**Historique des Absences :**
```
Tableau :
- Date de l'absence
- Type avec badge coloré
- Motif
- Date de déclaration
```

**Modèle de Données : Absence**
```java
- id : Long (PK)
- user : User (FK)
- absenceDate : LocalDate
- type : AbsenceType (ENUM)
- reason : String (TEXT)
- createdAt : LocalDateTime
```

### D. 🔍 Observations au Lieu de Travail (`/employee/observations`)

**Signalement d'un Problème :**
```
Formulaire :
  - Titre du problème
  - Description détaillée
  - Niveau de priorité

Priorités disponibles :
  1. Faible (🟢)
  2. Moyen (🟡)
  3. Élevé (🔴)
  4. Critique (🔴🔴)
↓
Système :
  - Crée une observation
  - Horodatage automatique (createdAt)
  - Statut initial : PENDING
  - Notifie les administrateurs (optionnel)
↓
Message : "Observation signalée avec succès. 
           Un administrateur sera notifié."
```

**Historique des Observations :**
```
Affichage amélioré :
  - Titre avec priorité (badge coloré)
  - Description complète
  - Date et heure du signalement
  - Statut actuel
  - Réponse de l'administrateur (si disponible)

Statuts possibles :
  1. En Attente (⏳)
  2. En Cours (⚙️)
  3. Résolu (✅)
  4. Fermé (📋)
```

**Modèle de Données : WorkplaceObservation**
```java
- id : Long (PK)
- user : User (FK)
- title : String
- description : String (TEXT)
- photoPath : String (optionnel)
- priority : Priority (ENUM)
- status : Status (ENUM)
- createdAt : LocalDateTime
- resolvedAt : LocalDateTime
- adminNotes : String
```

### E. 👤 Gestion du Profil (`/employee/profile`)

**Affichage des Informations :**
```
- Nom complet
- Email
- Téléphone
- Rôle
```

**Modification :**
```
Formulaire modifiable :
  - Prénom
  - Nom
  - Téléphone

Restrictions :
  - Email non modifiable (identifiant unique)
  - Mot de passe changé séparément
```

---

## 🛠️ Fonctionnalités Administrateur

### 1. Gestion des Employés (`/admin/employees`)

**Liste des Employés :**
```
Tableau affichant :
- Nom complet
- Email
- Téléphone
- Statut (Actif/Inactif)
- Indicateur première connexion (🔐)
- Actions (Profil, Modifier)
```

**Créer un Employé (`/admin/employees/new`):**
```
Formulaire :
  - Prénom
  - Nom
  - Email (unique)
  - Téléphone

Système génère automatiquement :
  - Mot de passe : temp123
  - Role : EMPLOYEE
  - firstLogin : true
  - enabled : true
```

**Modifier un Employé (`/admin/employees/{id}/edit`):**
```
Formulaire :
  - Prénom
  - Nom
  - Téléphone
  - Statut (Actif/Inactif)

Restrictions :
  - Email non modifiable
  - Mot de passe non accessible (utiliser réinitialisation)
```

### 2. Profil Détaillé de l'Employé (`/admin/employees/{id}/profile`)

**Informations Complètes :**
```
Section 1 : Identité
  - Nom complet
  - Email
  - Téléphone
  - Statut (Actif/Inactif)
  - Première connexion (En Attente / Complétée)
  - Date de création

Boutons d'action :
  - Voir les Pointages
  - Voir les Absences
  - Voir les Observations
  - Modifier l'Employé

Section 2 : Pointages Récents (10 derniers)
  Tableau : Date | Jour | Arrivée | Départ | Notes

Section 3 : Absences Récentes (10 dernières)
  Tableau : Date | Type | Motif

Section 4 : Observations Récentes (5 dernières)
  Affichage : Titre | Priorité | Statut | Description | Réponse Admin
```

### 3. Historiques Détaillés

#### Pointages Complets (`/admin/employees/{id}/time-tracking`)
```
Tableau complet avec :
- Date
- Jour de la semaine
- Heure d'arrivée
- Heure de départ
- Durée travaillée (calculée)
- Notes

Stats :
- Total de pointages
```

#### Absences Complètes (`/admin/employees/{id}/absences`)
```
Tableau complet avec :
- Date de l'absence
- Type (badge coloré)
- Motif
- Date de déclaration

Stats :
- Total d'absences
- Nombre par type
```

#### Observations Complètes (`/admin/employees/{id}/observations`)
```
Affichage détaillé pour chaque observation :
- Titre
- Priorité (badge)
- Statut (badge)
- Date de création
- Description
- Réponse de l'admin (si existante)
- Formulaire de réponse :
  * Nouveau statut
  * Notes de l'administrateur
```

### 4. Gestion des Observations

**Mise à Jour du Statut (`/admin/observations/{id}/status`):**
```
Formulaire en ligne :
  - Sélecteur de nouveau statut
  - Textarea pour réponse à l'employé
  - Bouton Mettre à jour

Système :
  - Met à jour le statut
  - Enregistre la réponse de l'admin
  - Si RESOLVED ou CLOSED : enregistre resolvedAt
  - Confirmation
```

---

## 🔐 Sécurité

### Authentification
```
- Utilise Spring Security
- UserDetailsService basé sur User entity
- BCrypt pour l'encodage des mots de passe
- Vérification du rôle EMPLOYEE
```

### Autorisation
```
Routes protégées :
  /employee/** → hasRole('EMPLOYEE')
  /admin/** → hasRole('ADMIN')
  
Login personnalisé :
  /admin-secret-access (pour admin)
  /employee-login (pour employé - à créer)
```

### Validation
```
- Email unique (niveau BD)
- Mot de passe minimum 6 caractères
- Validation des formulaires côté client et serveur
```

---

## 📦 Architecture et Structure

### Entités
```
Entity/
├── User.java (modifié)
│   ├── Role: ADMIN, EMPLOYEE, CLIENT
│   └── firstLogin: boolean
├── TimeTracking.java (nouvelle)
├── Absence.java (nouvelle)
└── WorkplaceObservation.java (nouvelle)
```

### Repositories
```
Repository/
├── UserRepository.java (existant)
├── TimeTrackingRepository.java (nouveau)
├── AbsenceRepository.java (nouveau)
└── WorkplaceObservationRepository.java (nouveau)
```

### Controllers
```
Controller/
├── AdminController.java (augmenté)
│   ├── /admin/employees (GET/POST)
│   ├── /admin/employees/{id}/edit (GET)
│   ├── /admin/employees/{id}/update (POST)
│   ├── /admin/employees/{id}/profile (GET)
│   ├── /admin/employees/{id}/time-tracking (GET)
│   ├── /admin/employees/{id}/absences (GET)
│   ├── /admin/employees/{id}/observations (GET)
│   └── /admin/observations/{id}/status (POST)
└── EmployeeController.java (nouveau)
    ├── /employee/dashboard (GET)
    ├── /employee/change-password (GET/POST)
    ├── /employee/time-tracking (GET/POST)
    ├── /employee/time-tracking/arrival (POST)
    ├── /employee/time-tracking/departure (POST)
    ├── /employee/absences (GET/POST)
    ├── /employee/observations (GET/POST)
    └── /employee/profile (GET/POST)
```

### Templates
```
templates/
├── employee/
│   ├── dashboard.html (vue d'ensemble)
│   ├── change-password.html (obligatoire)
│   ├── time-tracking.html (pointage)
│   ├── absences.html (déclaration)
│   ├── observations.html (signalements)
│   └── profile.html (profil)
└── admin/
    ├── employees.html (liste)
    ├── employee-form.html (création/modification)
    ├── employee-profile.html (vue complète)
    ├── employee-time-tracking.html (pointages)
    ├── employee-absences.html (absences)
    └── employee-observations.html (observations)
```

---

## 💾 Modèle de Données

### User (modifié)
```sql
ALTER TABLE users ADD COLUMN first_login BOOLEAN DEFAULT true;

ENUM Role:
  - ADMIN
  - EMPLOYEE
  - CLIENT
```

### TimeTracking (nouvelle table)
```sql
CREATE TABLE time_tracking (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  user_id BIGINT NOT NULL,
  tracking_date DATE NOT NULL,
  arrival_time TIME,
  departure_time TIME,
  notes VARCHAR(500),
  FOREIGN KEY (user_id) REFERENCES users(id)
);
```

### Absence (nouvelle table)
```sql
CREATE TABLE absences (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  user_id BIGINT NOT NULL,
  absence_date DATE NOT NULL,
  type VARCHAR(50) NOT NULL,
  reason TEXT,
  created_at TIMESTAMP,
  FOREIGN KEY (user_id) REFERENCES users(id)
);
```

### WorkplaceObservation (nouvelle table)
```sql
CREATE TABLE workplace_observations (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  user_id BIGINT NOT NULL,
  title VARCHAR(255) NOT NULL,
  description TEXT NOT NULL,
  photo_path VARCHAR(500),
  priority VARCHAR(50),
  status VARCHAR(50),
  created_at TIMESTAMP,
  resolved_at TIMESTAMP,
  admin_notes TEXT,
  FOREIGN KEY (user_id) REFERENCES users(id)
);
```

---

## 🎨 Interface Utilisateur

### Thème des Templates
```
Couleurs :
  - Primaire : #007bff (Bleu)
  - Succès : #28a745 (Vert)
  - Danger : #dc3545 (Rouge)
  - Warning : #ffc107 (Jaune)
  - Info : #17a2b8 (Cyan)

Composants :
  - Cards avec ombre (box-shadow)
  - Navbars pour navigation
  - Badges pour statuts
  - Tableaux responsifs
  - Formulaires structurés
```

### Navigation Employé
```
┌─────────────────────────────────────┐
│    BMS Clean Solution                  │
│ Dashboard | Pointage | Absences │ │
│ Observations | Profil | Déconnexion │
└─────────────────────────────────────┘
```

---

## 📋 Checklist d'Utilisation

### Pour l'Administrateur
- [ ] Accéder à `/admin/employees`
- [ ] Créer un nouvel employé via `/admin/employees/new`
- [ ] Transmettre email et mot de passe (temp123)
- [ ] Consulter le profil d'un employé via `/admin/employees/{id}/profile`
- [ ] Voir les pointages détaillés
- [ ] Voir les absences déclarées
- [ ] Consulter et répondre aux observations
- [ ] Modifier les informations d'un employé

### Pour l'Employé
- [ ] Se connecter avec identifiants fournis
- [ ] Changer le mot de passe obligatoirement
- [ ] Se reconnecter avec nouveau mot de passe
- [ ] Accéder au dashboard
- [ ] Enregistrer arrivée et départ
- [ ] Consulter historique pointages
- [ ] Déclarer une absence si nécessaire
- [ ] Signaler une observation
- [ ] Mettre à jour profil
- [ ] Consulter réponses admin

---

## ⚙️ Configuration et Déploiement

### Prérequis
- Java 11+
- Spring Boot 2.7+
- Base de données (H2 pour dev, MySQL pour prod)
- Thymeleaf pour les templates

### Variables d'Environnement
```properties
# SecurityConfig
spring.security.user.name=admin
spring.security.user.password=admin123

# Database
spring.jpa.hibernate.ddl-auto=update
spring.datasource.url=jdbc:mysql://localhost:3306/rapidclean
```

### Migration Base de Données
```sql
-- Les entités JPA créeront automatiquement les tables
-- Hibernateur : spring.jpa.hibernate.ddl-auto=update
```

---

## 🐛 Dépannage

### Problème : Employé coincé sur page changement mot de passe
**Solution :** Vérifier que `firstLogin = false` après modification

### Problème : Email non unique
**Solution :** Contrainte UNIQUE en base de données - vérifier doublon

### Problème : Mot de passe non accepté
**Solution :** Minimum 6 caractères requis

### Problème : Observations non visibles
**Solution :** Vérifier que l'employé est bien créé et connecté

---

## 📝 Notes Importantes

1. **Sécurité des Mots de Passe**
   - Toujours changer temp123 à la première connexion
   - Mots de passe encodés en BCrypt
   - Jamais stockés en clair

2. **Horodatage**
   - Basé sur `LocalDateTime.now()` du serveur
   - Fuseau horaire : système serveur

3. **Performances**
   - Pointages triés par date décroissante
   - Limiteurs affichage (10 derniers pointages, etc.)
   - Requêtes optimisées avec JPA

4. **Conformité RGPD**
   - Données employés sécurisées
   - Accès contrôlé par rôle
   - Audit possible via logs

---

## 📞 Support

Pour toute question ou problème :
- Consulter les logs de l'application
- Vérifier base de données
- Valider configuration SecurityConfig
- Tester routes avec Postman/Insomnia

---

**Version:** 1.0  
**Date:** Décembre 2025  
**Auteur:** Système Automatisé BMS Rapid Clean
