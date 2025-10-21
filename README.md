# BMS Rapid Clean - Service de Nettoyage 24h/24

## Description
BMS Rapid Clean est une application web moderne développée avec Spring Boot pour gérer les services de nettoyage professionnel disponibles 24h/24. L'application offre une interface élégante et moderne avec des fonctionnalités complètes pour les administrateurs et les clients.

## Fonctionnalités

### 🏠 Interface Client
- **Inscription/Connexion** : Système d'authentification sécurisé
- **Tableau de bord** : Vue d'ensemble des demandes et statistiques
- **Demande de service** : Formulaire intuitif pour créer des demandes
- **Gestion du profil** : Modification des informations personnelles
- **Suivi des demandes** : Statut en temps réel des services

### 🔧 Interface Administrateur
- **Tableau de bord admin** : Statistiques et vue d'ensemble
- **Gestion des services** : CRUD complet des services disponibles
- **Gestion des demandes** : Suivi et mise à jour du statut
- **Gestion des utilisateurs** : Administration des comptes clients
- **Rapports** : Statistiques et analyses

### 🎨 Design et UX
- **Design moderne** : Interface élégante avec animations CSS
- **Responsive** : Compatible mobile, tablette et desktop
- **Couleurs cohérentes** : Palette de couleurs professionnelle
- **Logo et bannière** : Identité visuelle unifiée
- **Animations fluides** : Transitions et effets visuels

## Technologies Utilisées

### Backend
- **Spring Boot 3.2.0** : Framework principal
- **Spring Security** : Authentification et autorisation
- **Spring Data JPA** : Gestion des données
- **Thymeleaf** : Moteur de templates
- **MySQL** : Base de données
- **Maven** : Gestion des dépendances

### Frontend
- **Bootstrap 5.3.0** : Framework CSS
- **Font Awesome 6.4.0** : Icônes
- **Google Fonts (Poppins)** : Typographie
- **CSS3** : Animations et styles personnalisés
- **JavaScript ES6+** : Interactivité

## Installation et Configuration

### Prérequis
- Java 17 ou supérieur
- Maven 3.6 ou supérieur
- PostgreSQL 12 ou supérieur
- IDE (IntelliJ IDEA, Eclipse, VS Code)

### Configuration de la Base de Données

1. **Installer PostgreSQL :**
```bash
# Ubuntu/Debian
sudo apt-get install postgresql postgresql-contrib

# CentOS/RHEL
sudo yum install postgresql postgresql-server

# macOS avec Homebrew
brew install postgresql
```

2. **Démarrer PostgreSQL :**
```bash
# Ubuntu/Debian
sudo systemctl start postgresql

# CentOS/RHEL
sudo systemctl start postgresql

# macOS
brew services start postgresql
```

3. **Créer la base de données :**
```sql
CREATE DATABASE rapidclean;
CREATE USER postgres WITH PASSWORD 'postgres';
GRANT ALL PRIVILEGES ON DATABASE rapidclean TO postgres;
```

### Installation

1. **Cloner le projet :**
```bash
git clone <url-du-repo>
cd rapidClean
```

2. **Installer les dépendances :**
```bash
mvn clean install
```

3. **Lancer l'application :**
```bash
# Option 1: Script final (recommandé)
./start-final.sh

# Option 2: Avec H2 (développement rapide)
./start-h2.sh

# Option 3: Avec PostgreSQL (si configuré)
./start-postgres.sh

# Option 4: Lancement manuel
mvn spring-boot:run
```

4. **Accéder à l'application :**
```
http://localhost:8999
```

### 🚀 Options de Démarrage

**Script Final (Recommandé) :**
```bash
./start-final.sh
```
- Essaie PostgreSQL en premier
- Bascule automatiquement sur H2 si PostgreSQL n'est pas disponible
- Configuration automatique et gestion d'erreurs

**Avec H2 (Développement Rapide) :**
```bash
./start-h2.sh
```
- Base de données en mémoire
- Aucune configuration requise
- Parfait pour le développement et les tests
- Console H2 accessible sur `/h2-console`

**Avec PostgreSQL (Production) :**
```bash
# Si PostgreSQL est configuré
./start-postgres.sh

# Sinon, consultez setup-postgres-manual.md pour la configuration
```
- Base de données persistante
- Configuration requise (voir `setup-postgres-manual.md`)
- Idéal pour la production

### 🔧 Configuration PostgreSQL

Si vous voulez utiliser PostgreSQL, utilisez le script d'aide :

```bash
./fix-postgres.sh
```

Ce script vous donnera plusieurs méthodes pour configurer PostgreSQL. Consultez aussi `setup-postgres-manual.md` pour les instructions détaillées.

## Structure du Projet

```
src/
├── main/
│   ├── java/com/rapidclean/
│   │   ├── entity/          # Entités JPA
│   │   ├── repository/       # Repositories
│   │   ├── controller/      # Contrôleurs
│   │   ├── security/        # Configuration sécurité
│   │   └── RapidCleanApplication.java
│   ├── resources/
│   │   ├── static/
│   │   │   ├── css/         # Styles personnalisés
│   │   │   ├── js/          # JavaScript
│   │   │   └── images/      # Images et logo
│   │   ├── templates/       # Templates Thymeleaf
│   │   └── application.yml  # Configuration
│   └── test/               # Tests
```

## Utilisation

### Comptes par Défaut
L'application crée automatiquement les tables nécessaires au premier démarrage.

### Création d'un Compte Admin
Pour créer un compte administrateur, vous pouvez :
1. Utiliser l'interface d'inscription puis modifier le rôle en base
2. Créer directement en base de données

### Pages Principales
- **Landing Page** : `/` - Page d'accueil avec sections complètes
- **Services** : `/services` - Liste des services disponibles
- **Avis** : `/reviews` - Section avis clients avec formulaire
- **Contact** : `/contact` - Formulaire de contact amélioré
- **À Propos** : `/about` - Informations sur l'entreprise
- **Connexion** : `/login` - Page de connexion
- **Inscription** : `/register` - Page d'inscription

### Interface Client
- **Tableau de bord** : `/client/dashboard`
- **Demande de service** : `/client/request`
- **Profil** : `/client/profile`

### Interface Admin
- **Tableau de bord** : `/admin/dashboard` - Vue d'ensemble avec notifications
- **Gestion services** : `/admin/services` - CRUD des services
- **Gestion demandes** : `/admin/requests` - Suivi des demandes
- **Gestion utilisateurs** : `/admin/users` - Administration des comptes
- **Gestion messages** : `/admin/messages` - Messages de contact
- **Gestion avis** : `/admin/reviews` - Validation des avis clients

## Personnalisation

### Couleurs
Les couleurs principales sont définies dans `src/main/resources/static/css/style.css` :
```css
:root {
    --primary-color: #2c5aa0;
    --secondary-color: #1e3a5f;
    --accent-color: #4a90e2;
    --success-color: #28a745;
    --warning-color: #ffc107;
    --danger-color: #dc3545;
}
```

### Logo
Le logo SVG est situé dans `src/main/resources/static/images/logo.svg` et peut être personnalisé.

### Animations
Les animations CSS sont définies dans le fichier `style.css` avec des classes comme :
- `.animate-fade-in`
- `.animate-slide-up`
- `.animate-float`

## Déploiement

### Production
1. **Configurer la base de données de production**
2. **Modifier `application.yml` pour la production**
3. **Construire l'application :**
```bash
mvn clean package
```
4. **Déployer le fichier JAR généré**

### Variables d'Environnement
- `MAIL_USERNAME` : Email pour l'envoi de notifications
- `MAIL_PASSWORD` : Mot de passe email
- `DB_URL` : URL de la base de données
- `DB_USERNAME` : Utilisateur base de données
- `DB_PASSWORD` : Mot de passe base de données

## Fonctionnalités Avancées

### Notifications Email
L'application est configurée pour envoyer des emails (nécessite configuration SMTP).

### Sécurité
- Authentification par email/mot de passe
- Sessions sécurisées
- Protection CSRF
- Validation des données

### Responsive Design
- Mobile-first approach
- Breakpoints Bootstrap
- Navigation mobile optimisée

## Support et Maintenance

### Logs
Les logs sont configurés dans `application.yml` avec différents niveaux selon l'environnement.

### Monitoring
L'application inclut Spring Boot Actuator pour le monitoring en production.

### Sauvegarde
Il est recommandé de configurer des sauvegardes régulières de la base de données MySQL.

## Contribution

1. Fork le projet
2. Créer une branche feature (`git checkout -b feature/nouvelle-fonctionnalite`)
3. Commit les changements (`git commit -am 'Ajouter nouvelle fonctionnalité'`)
4. Push vers la branche (`git push origin feature/nouvelle-fonctionnalite`)
5. Créer une Pull Request

## Licence

Ce projet est sous licence MIT. Voir le fichier `LICENSE` pour plus de détails.

## Contact

Pour toute question ou support :
- Email : info@bmsrapidclean.com
- Téléphone : +1 (555) 123-4567
- Site web : https://bmsrapidclean.com
