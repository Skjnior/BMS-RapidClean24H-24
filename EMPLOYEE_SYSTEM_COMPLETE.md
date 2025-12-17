# 📋 Système de Gestion des Employés - Documentation Complète

## 1️⃣ Processus d'Inscription et Première Connexion

### Pour l'Administrateur:
1. **Accédez au panel admin** : `/admin/employees`
2. **Cliquez sur "Créer un Employé"**
3. **Remplissez le formulaire** avec:
   - Prénom et Nom
   - Email unique
   - Téléphone
   - Rôle (fixé à "EMPLOYEE" automatiquement)
   - Statut (Actif/Inactif)

4. **Le système génère automatiquement**:
   - Un mot de passe par défaut: **`temp123`**
   - Une inscription en BD avec `first_login = true`
   - Un message de confirmation avec les identifiants

### Pour l'Employé (Première Connexion):
1. **Allez à** `/employee-login`
2. **Connectez-vous avec**:
   - Email: celui fourni par l'admin
   - Mot de passe: `temp123`

3. **Accès automatique obligatoire au changement de mot de passe**:
   - L'intercepteur `EmployeeFirstLoginInterceptor` redirige vers `/employee/change-password`
   - Toute tentative d'accès à `/employee/**` force la redirection
   - Changement de mot de passe obligatoire avant d'accéder aux autres fonctionnalités

4. **Après avoir changé le mot de passe**:
   - Cliquez sur "Modifier et continuer"
   - L'employé est **automatiquement déconnecté** (`/logout`)
   - Redirection vers la page d'accueil

5. **Reconnexion avec nouveau mot de passe**:
   - Retournez à `/employee-login`
   - Connectez-vous avec le nouveau mot de passe
   - Accès complet au dashboard employé

---

## 2️⃣ Fonctionnalités de l'Employé (Après Première Connexion)

### A. Dashboard Employé (`/employee/dashboard`)
**Vue d'ensemble** avec:
- Pointage du jour (heure d'arrivée/départ)
- Dernières absences déclarées (mois en cours)
- Dernier signalement d'observation
- Raccourcis rapides vers les fonctionnalités

### B. Pointage des Heures (`/employee/time-tracking`)

**Enregistrement automatique avec horodatage**

#### Heure d'Arrivée:
1. Cliquez sur **"Enregistrer Arrivée"**
2. L'heure actuelle est enregistrée automatiquement
3. Message de confirmation: "Heure d'arrivée enregistrée à HH:MM"

#### Heure de Départ:
1. Cliquez sur **"Enregistrer Départ"**
   - ⚠️ L'arrivée doit d'abord être enregistrée
2. L'heure actuelle est enregistrée
3. Message de confirmation: "Heure de départ enregistrée à HH:MM"

#### Historique:
- Vue de tous les pointages de l'employé
- Affichage par date décroissante
- Calcul automatique du temps travaillé

### C. Déclaration d'Absence (`/employee/absences`)

**Justification des absences avec motif**

#### Déclarer une Absence:
1. **Date de l'absence**: Sélectionnez la date du jour absent
2. **Type d'absence**: Choisissez parmi:
   - Congé payé
   - Arrêt maladie
   - Congé sans solde
   - Absence justifiée
   - Absence injustifiée

3. **Motif/Raison**: Ajoutez une description (ex: rendez-vous médical, urgence familiale)
4. Cliquez sur **"Déclarer l'Absence"**
5. Confirmation: "Absence déclarée avec succès"

#### Historique:
- Tous les signalements d'absence de l'employé
- Affichage décroissant par date
- Détails: type, date, motif

### D. Observations au Lieu de Travail (`/employee/observations`)

**Signalement de problèmes avec description et priorité**

#### Signaler une Observation:
1. **Titre du problème**: (ex: "Matériel endommagé", "Problème de climatisation")
2. **Description détaillée**: Explicitez le problème observé
3. **Priorité**: Sélectionnez le niveau d'urgence:
   - 🔴 CRITIQUE (défaut de sécurité)
   - 🟠 HAUTE (affecte la productivité)
   - 🟡 MOYENNE (inconvénient mineur)
   - 🟢 BASSE (amélioration cosmétique)

4. Cliquez sur **"Soumettre le Signalement"**
5. Message: "Observation signalée avec succès. Un administrateur sera notifié."

#### Historique:
- Tous les signalements de l'employé
- Affichage décroissant par date de création (horodaté)
- Statut: EN ATTENTE / EN COURS / RÉSOLU

---

## 3️⃣ Architecture Technique

### Base de Données

#### Table `users` (Entité User)
```
- id (PK)
- first_name, last_name
- email (UNIQUE)
- password (BCrypt)
- phone
- role (ENUM: ADMIN, EMPLOYEE, CLIENT)
- enabled (BOOLEAN)
- first_login (BOOLEAN) <- Obligatoire à true lors de la création d'employé
- created_at (TIMESTAMP)
```

#### Table `time_tracking`
```
- id (PK)
- user_id (FK)
- tracking_date (DATE)
- arrival_time (TIME)
- departure_time (TIME)
- created_at (TIMESTAMP)
```

#### Table `absences`
```
- id (PK)
- user_id (FK)
- absence_date (DATE)
- type (ENUM: PAID_LEAVE, SICK_LEAVE, UNPAID_LEAVE, JUSTIFIED, UNJUSTIFIED)
- reason (TEXT)
- created_at (TIMESTAMP)
```

#### Table `workplace_observations`
```
- id (PK)
- user_id (FK)
- title (VARCHAR)
- description (TEXT)
- priority (ENUM: LOW, MEDIUM, HIGH, CRITICAL)
- status (ENUM: PENDING, IN_PROGRESS, RESOLVED)
- created_at (TIMESTAMP)
- updated_at (TIMESTAMP)
```

### Composants Clés

#### 1. Interceptor: `EmployeeFirstLoginInterceptor`
- **Effet**: Force les employés avec `firstLogin=true` à se reconnecter à `/employee/change-password`
- **Protection**: Empêche l'accès à `/employee/**` sauf `/employee/change-password`
- **Enregistré**: Via `WebConfig`

#### 2. CommandLineRunner: `SchemaInitializationRunner`
- **Rôle**: Initialise le schéma BD au démarrage
  - Crée la colonne `first_login` si manquante
  - Corrige la contrainte CHECK pour accepter le rôle EMPLOYEE
  - Mises à jour idempotentes et sûres
- **Ordre**: Order(0) pour s'exécuter avant autres runners

#### 3. Contrôleurs
- **LoginController**: Routes `/employee-login` et `/admin-login`
- **EmployeeController**: Toutes les routes `/employee/**`
- **AdminController**: CRUD employé et admin pages

#### 4. Security
- **Spring Security** avec authentication par email/password (BCrypt)
- **Logout URL**: `/logout` → redirige vers `/`
- **Roles**: ADMIN, EMPLOYEE, CLIENT

---

## 4️⃣ Flux Complet (De Bout en Bout)

```
ADMIN                           EMPLOYEE                       SYSTÈME
  │                                │                              │
  ├─ Va à /admin/employees         │                              │
  │  (liste des employés)          │                              │
  │                                │                              │
  ├─ Clique "Créer Employé"        │                              │
  │  (formulaire)                  │                              │
  │                                │                              │
  ├─ Remplit:                      │                              │
  │  - Prénom: Mamadou             │                              │
  │  - Nom: Ly                      │                              │
  │  - Email: mamadou@example.com   │                              │
  │  - Téléphone: +1234567890       │                              │
  │  (Rôle = EMPLOYEE auto)         │                              │
  │                                │                              │
  ├─ Soumet le formulaire          │                              │
  │                                │                              ├─ Crée User:
  │                                │                              │  - firstLogin=true
  │                                │                              │  - password=BCrypt("temp123")
  │                                │                              │  - role=EMPLOYEE
  │                                │                              │
  │                                │ Reçoit identifiants:
  │                                │ Email: mamadou@example.com
  │                                │ Pass: temp123
  │                                │
  │                                ├─ Va à /employee-login
  │                                │
  │                                ├─ Login form:
  │                                │  - Email: mamadou@example.com
  │                                │  - Password: temp123
  │                                │
  │                                ├─ Soumet
  │                                │
  │                                │                              ├─ Authentifie ✓
  │                                │                              ├─ EmployeeFirstLoginInterceptor
  │                                │                              │  détecte firstLogin=true
  │                                │                              │
  │                                ├──────────────────────────────┤
  │                                ← Redirection FORCE
  │                                /employee/change-password
  │                                │
  │                                ├─ Change Password Form
  │                                │
  │                                ├─ Nouveau mot de passe: "MyNewPass123"
  │                                │ Confirmation: "MyNewPass123"
  │                                │
  │                                ├─ Soumet
  │                                │
  │                                │                              ├─ Update User:
  │                                │                              │  - password=BCrypt("MyNewPass123")
  │                                │                              │  - firstLogin=false
  │                                │                              │
  │                                │                              ├─ Redirect: /logout
  │                                │                              │
  │                                ├──────────────────────────────┤
  │                                ← Logout (session invalidée)
  │                                ← Redirection: / (page d'accueil)
  │                                │
  │                                ├─ Va de nouveau à /employee-login
  │                                │
  │                                ├─ Login avec nouveau mdp:
  │                                │  - Email: mamadou@example.com
  │                                │  - Password: MyNewPass123
  │                                │
  │                                ├─ Soumet
  │                                │
  │                                │                              ├─ Authentifie ✓
  │                                │                              ├─ firstLogin=false
  │                                │                              │  (pas de redirection)
  │                                │
  │                                ├──────────────────────────────┤
  │                                ← Accès: /employee/dashboard
  │                                │ (fonctionnalités ouvertes)
  │                                │
```

---

## 5️⃣ Points d'Entrée Clés

| Rôle | URL | Fonctionnalité |
|------|-----|----------------|
| Public | `/employee-login` | Login employé |
| Admin | `/admin/employees` | CRUD employés |
| Admin | `/admin/employees/new` | Créer employé |
| Admin | `/admin/employees/{id}/edit` | Modifier employé |
| Admin | `/admin/employees/{id}/delete` | Supprimer employé |
| Employé | `/employee/dashboard` | Vue d'ensemble |
| Employé | `/employee/time-tracking` | Pointage heures |
| Employé | `/employee/absences` | Déclaration absence |
| Employé | `/employee/observations` | Signalement problèmes |
| Employé | `/employee/profile` | Profil personnel |
| Tous | `/logout` | Déconnexion |

---

## 6️⃣ Checklist de Vérification

- [x] **Admin peut créer des employés** (`/admin/employees` POST)
- [x] **Mot de passe par défaut `temp123`** créé automatiquement
- [x] **Colonne `first_login` créée** et gérée au démarrage
- [x] **Employé force au changement de mot de passe** (interceptor)
- [x] **Auto-déconnexion après changement** (`/logout`)
- [x] **Employé peut se reconnecter** avec nouveau mot de passe
- [x] **Pointage heures** (arrivée + départ avec horodatage)
- [x] **Déclaration absence** (type + motif)
- [x] **Signalement observations** (titre + description + priorité)
- [x] **Dashboard employé** avec vue d'ensemble
- [x] **Historique des actions** (pointages, absences, observations)

---

## 7️⃣ Dépannage

### Problème: Employé non trouvé après création
**Solution**: Redémarrez l'application. La colonne `first_login` est créée via `SchemaInitializationRunner`.

### Problème: Contrainte CHECK sur le rôle EMPLOYEE
**Solution**: Redémarrez l'application. `SchemaInitializationRunner` corrige automatiquement la contrainte.

### Problème: Interceptor non appliqué
**Solution**: Vérifiez que `WebConfig` est enregistré avec `@Configuration` et que le bean est créé.

### Problème: `/logout` ne fonctionne pas
**Solution**: Vérifiez la configuration Spring Security dans `SecurityConfig`. L'endpoint `/logout` est standard.

---

## 8️⃣ Notes de Sécurité

✅ **Mot de passe**:
- Stocké avec BCrypt (coût 10)
- Jamais en texte brut
- Changement forcé à la première connexion

✅ **Authentification**:
- Par email (principal name)
- Spring Security authentication chain
- Session-based avec Spring Security

✅ **Autorisation**:
- Rôles: ADMIN, EMPLOYEE, CLIENT
- Endpoints protégés par `/admin/**` (ADMIN) et `/employee/**` (EMPLOYEE)

⚠️ **À faire avant production**:
- Retirer/sécuriser les endpoints `/api/debug/**`
- Activer HTTPS
- Configurer CSRF token (actuellement désactivé)
- Implémenter rate limiting sur login
- Ajouter logging d'audit pour les actions sensibles

---

**Dernière mise à jour**: 13 Décembre 2025
**Version**: 1.0 (Système complet implémenté)
