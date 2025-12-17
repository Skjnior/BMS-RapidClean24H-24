# 🗄️ STRUCTURE DE LA BASE DE DONNÉES BMS RAPID CLEAN

## 📊 **RÉSUMÉ GÉNÉRAL**

**Base de données :** PostgreSQL  
**Nom de la base :** `rapidclean`  
**Utilisateur :** `kaba`  
**Nombre de tables :** 6 tables principales  
**État :** ✅ Base de données opérationnelle avec 2 utilisateurs créés

---

## 📋 **TABLES DISPONIBLES**

### 1. **`users`** - Table des utilisateurs
**Rôle :** Gestion des utilisateurs (Admin/Client)  
**Enregistrements :** 2 utilisateurs  
**Champs principaux :**
- `id` (bigint, PK, auto-incrément)
- `first_name` (varchar) - Prénom
- `last_name` (varchar) - Nom
- `email` (varchar, unique) - Email (utilisé pour la connexion)
- `password` (varchar) - Mot de passe crypté (BCrypt)
- `phone` (varchar) - Téléphone
- `role` (varchar) - Rôle (ADMIN/CLIENT)
- `enabled` (boolean) - Compte activé
- `created_at` (timestamp) - Date de création

**Utilisateurs existants :**
- **Admin :** `admin@bmsrapidclean.com` / `admin123`
- **Client :** `client@example.com` / `client123`

---

### 2. **`services`** - Table des services
**Rôle :** Catalogue des services proposés  
**Enregistrements :** 0 (vide)  
**Champs principaux :**
- `id` (bigint, PK, auto-incrément)
- `name` (varchar) - Nom du service
- `description` (text) - Description détaillée
- `price` (double) - Prix du service
- `image_url` (varchar) - URL de l'image
- `active` (boolean) - Service actif
- `created_at` (timestamp) - Date de création

---

### 3. **`service_requests`** - Table des demandes de service
**Rôle :** Demandes de service des clients  
**Enregistrements :** 0 (vide)  
**Champs principaux :**
- `id` (bigint, PK, auto-incrément)
- `user_id` (bigint, FK) - Référence vers `users.id`
- `service_type` (varchar) - Type de service demandé
- `address` (varchar) - Adresse d'intervention
- `description` (varchar) - Description de la demande
- `service_date` (timestamp) - Date souhaitée
- `status` (varchar) - Statut (PENDING/CONFIRMED/IN_PROGRESS/COMPLETED/CANCELLED)
- `price` (double) - Prix convenu
- `created_at` (timestamp) - Date de création
- `updated_at` (timestamp) - Dernière modification

---

### 4. **`contact_messages`** - Table des messages de contact
**Rôle :** Messages reçus via le formulaire de contact  
**Enregistrements :** 0 (vide)  
**Champs principaux :**
- `id` (bigint, PK, auto-incrément)
- `first_name` (varchar) - Prénom
- `last_name` (varchar) - Nom
- `email` (varchar) - Email
- `phone` (varchar) - Téléphone
- `subject` (varchar) - Sujet du message
- `message` (text) - Contenu du message
- `status` (varchar) - Statut (NEW/READ/REPLIED/CLOSED)
- `read` (boolean) - Message lu
- `read_at` (timestamp) - Date de lecture
- `created_at` (timestamp) - Date de création

---

### 5. **`reviews`** - Table des avis clients
**Rôle :** Avis et évaluations des clients  
**Enregistrements :** 0 (vide)  
**Champs principaux :**
- `id` (bigint, PK, auto-incrément)
- `user_id` (bigint, FK) - Référence vers `users.id` (optionnel)
- `customer_name` (varchar) - Nom du client
- `customer_email` (varchar) - Email du client
- `rating` (integer) - Note (1-5 étoiles)
- `comment` (text) - Commentaire
- `approved` (boolean) - Avis approuvé par l'admin
- `created_at` (timestamp) - Date de création

---

### 6. **`notifications`** - Table des notifications
**Rôle :** Notifications système pour l'admin  
**Enregistrements :** 0 (vide)  
**Champs principaux :**
- `id` (bigint, PK, auto-incrément)
- `title` (varchar) - Titre de la notification
- `message` (text) - Contenu de la notification
- `type` (varchar) - Type (NEW_MESSAGE/NEW_REVIEW/NEW_REQUEST/SYSTEM)
- `priority` (varchar) - Priorité (LOW/MEDIUM/HIGH/URGENT)
- `read` (boolean) - Notification lue
- `read_at` (timestamp) - Date de lecture
- `created_at` (timestamp) - Date de création

---

## 🔗 **RELATIONS ENTRE TABLES**

### **Clés étrangères identifiées :**

1. **`service_requests.user_id`** → **`users.id`**
   - Une demande de service appartient à un utilisateur

2. **`reviews.user_id`** → **`users.id`**
   - Un avis peut être lié à un utilisateur (optionnel)

---

## 📈 **ÉTAT ACTUEL DE LA BASE**

### **Données existantes :**
- ✅ **2 utilisateurs** créés (1 admin + 1 client)
- ❌ **0 services** dans le catalogue
- ❌ **0 demandes** de service
- ❌ **0 messages** de contact
- ❌ **0 avis** clients
- ❌ **0 notifications**

### **Recommandations :**
1. **Ajouter des services** dans le catalogue
2. **Créer des données de test** pour les demandes
3. **Ajouter des avis** d'exemple
4. **Tester le formulaire de contact**

---

## 🛠️ **COMMANDES UTILES**

### **Connexion directe à la base :**
```bash
PGPASSWORD='toor' psql -h localhost -p 5432 -U kaba -d rapidclean
```

### **Commandes PostgreSQL utiles :**
- `\dt` - Lister toutes les tables
- `\d table_name` - Décrire une table
- `SELECT * FROM table_name;` - Voir le contenu
- `\q` - Quitter

---

## 🎯 **PROCHAINES ÉTAPES SUGGÉRÉES**

1. **Remplir le catalogue de services**
2. **Tester la création de demandes**
3. **Ajouter des données d'exemple**
4. **Vérifier le dashboard admin**
5. **Tester le formulaire de contact**

---

*Rapport généré le : $(date)*
*Base de données : PostgreSQL*
*Application : BMS Clean Solutions v1.0.0*
