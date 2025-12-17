# 🚀 Guide de Déploiement - BMS RapidClean

## 📋 Prérequis

### Serveur
- **OS** : Linux (Ubuntu 20.04+ recommandé) ou Windows Server
- **RAM** : Minimum 2GB, recommandé 4GB+
- **Disque** : Minimum 20GB d'espace libre
- **Java** : JDK 17 ou supérieur
- **PostgreSQL** : Version 12 ou supérieure
- **Maven** : Version 3.6+ (pour la compilation)

### Logiciels Requis
```bash
# Ubuntu/Debian
sudo apt update
sudo apt install openjdk-17-jdk postgresql maven git

# Vérifier les versions
java -version
mvn -version
psql --version
```

## 🔧 Configuration de la Base de Données

### 1. Créer la Base de Données PostgreSQL

```bash
# Se connecter à PostgreSQL
sudo -u postgres psql

# Créer la base de données et l'utilisateur
CREATE DATABASE rapidclean;
CREATE USER rapidclean_user WITH PASSWORD 'VOTRE_MOT_DE_PASSE_SECURISE';
GRANT ALL PRIVILEGES ON DATABASE rapidclean TO rapidclean_user;
\q
```

### 2. Configurer les Variables d'Environnement

Créez un fichier `.env` ou configurez les variables d'environnement système :

```bash
# Base de données
export DB_URL=jdbc:postgresql://localhost:5432/rapidclean
export DB_USERNAME=rapidclean_user
export DB_PASSWORD=VOTRE_MOT_DE_PASSE_SECURISE

# Application
export SPRING_PROFILES_ACTIVE=prod
export SERVER_PORT=8997

# Backup
export APP_BACKUP_ENABLED=true
export APP_BACKUP_DIRECTORY=/var/backups/rapidclean
export APP_BACKUP_RETENTION_DAYS=30

# Upload
export APP_UPLOAD_DIR=/var/uploads/rapidclean/observations
```

## 📦 Compilation et Packaging

### 1. Cloner le Projet

```bash
git clone <repository-url>
cd rapidClean
```

### 2. Compiler le Projet

```bash
# Compiler sans les tests (plus rapide)
mvn clean package -DskipTests

# Ou avec les tests
mvn clean package
```

Le fichier JAR sera créé dans : `target/rapid-clean-0.0.1-SNAPSHOT.jar`

## 🚀 Déploiement

### Option 1 : Déploiement avec systemd (Recommandé)

#### 1. Créer le Service Systemd

Créez le fichier `/etc/systemd/system/rapidclean.service` :

```ini
[Unit]
Description=BMS RapidClean Application
After=network.target postgresql.service

[Service]
Type=simple
User=rapidclean
Group=rapidclean
WorkingDirectory=/opt/rapidclean
ExecStart=/usr/bin/java -jar /opt/rapidclean/rapid-clean-0.0.1-SNAPSHOT.jar
Restart=always
RestartSec=10
StandardOutput=journal
StandardError=journal
SyslogIdentifier=rapidclean

# Variables d'environnement
Environment="SPRING_PROFILES_ACTIVE=prod"
Environment="DB_URL=jdbc:postgresql://localhost:5432/rapidclean"
Environment="DB_USERNAME=rapidclean_user"
Environment="DB_PASSWORD=VOTRE_MOT_DE_PASSE"

[Install]
WantedBy=multi-user.target
```

#### 2. Créer l'Utilisateur et les Répertoires

```bash
# Créer l'utilisateur
sudo useradd -r -s /bin/false rapidclean

# Créer les répertoires
sudo mkdir -p /opt/rapidclean
sudo mkdir -p /var/backups/rapidclean
sudo mkdir -p /var/uploads/rapidclean/observations
sudo mkdir -p /var/log/rapidclean

# Copier le JAR
sudo cp target/rapid-clean-0.0.1-SNAPSHOT.jar /opt/rapidclean/

# Définir les permissions
sudo chown -R rapidclean:rapidclean /opt/rapidclean
sudo chown -R rapidclean:rapidclean /var/backups/rapidclean
sudo chown -R rapidclean:rapidclean /var/uploads/rapidclean
```

#### 3. Démarrer le Service

```bash
# Recharger systemd
sudo systemctl daemon-reload

# Démarrer le service
sudo systemctl start rapidclean

# Activer au démarrage
sudo systemctl enable rapidclean

# Vérifier le statut
sudo systemctl status rapidclean

# Voir les logs
sudo journalctl -u rapidclean -f
```

### Option 2 : Déploiement avec Docker (Alternative)

Créez un `Dockerfile` :

```dockerfile
FROM openjdk:17-jdk-slim

WORKDIR /app

COPY target/rapid-clean-0.0.1-SNAPSHOT.jar app.jar

EXPOSE 8997

ENTRYPOINT ["java", "-jar", "app.jar"]
```

Et un `docker-compose.yml` :

```yaml
version: '3.8'

services:
  app:
    build: .
    ports:
      - "8997:8997"
    environment:
      - SPRING_PROFILES_ACTIVE=prod
      - DB_URL=jdbc:postgresql://db:5432/rapidclean
      - DB_USERNAME=rapidclean_user
      - DB_PASSWORD=${DB_PASSWORD}
    depends_on:
      - db
    volumes:
      - ./backups:/var/backups/rapidclean
      - ./uploads:/var/uploads/rapidclean

  db:
    image: postgres:15
    environment:
      - POSTGRES_DB=rapidclean
      - POSTGRES_USER=rapidclean_user
      - POSTGRES_PASSWORD=${DB_PASSWORD}
    volumes:
      - postgres_data:/var/lib/postgresql/data

volumes:
  postgres_data:
```

## 🔒 Configuration HTTPS avec Nginx (Recommandé)

### 1. Installer Nginx et Certbot

```bash
sudo apt install nginx certbot python3-certbot-nginx
```

### 2. Configuration Nginx

Créez `/etc/nginx/sites-available/rapidclean` :

```nginx
server {
    listen 80;
    server_name votre-domaine.com;

    # Redirection HTTPS
    return 301 https://$server_name$request_uri;
}

server {
    listen 443 ssl http2;
    server_name votre-domaine.com;

    # Certificats SSL (générés par Certbot)
    ssl_certificate /etc/letsencrypt/live/votre-domaine.com/fullchain.pem;
    ssl_certificate_key /etc/letsencrypt/live/votre-domaine.com/privkey.pem;

    # Configuration SSL
    ssl_protocols TLSv1.2 TLSv1.3;
    ssl_ciphers HIGH:!aNULL:!MD5;
    ssl_prefer_server_ciphers on;

    # Headers de sécurité
    add_header X-Frame-Options "DENY" always;
    add_header X-Content-Type-Options "nosniff" always;
    add_header X-XSS-Protection "1; mode=block" always;

    # Proxy vers l'application Spring Boot
    location / {
        proxy_pass http://localhost:8997;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
        proxy_set_header X-Forwarded-Proto $scheme;
        
        # WebSocket support
        proxy_http_version 1.1;
        proxy_set_header Upgrade $http_upgrade;
        proxy_set_header Connection "upgrade";
    }

    # Cache pour les assets statiques
    location ~* \.(css|js|jpg|jpeg|png|gif|ico|svg|woff|woff2|ttf|eot)$ {
        proxy_pass http://localhost:8997;
        expires 1y;
        add_header Cache-Control "public, immutable";
    }
}
```

### 3. Activer le Site et Obtenir le Certificat SSL

```bash
# Activer le site
sudo ln -s /etc/nginx/sites-available/rapidclean /etc/nginx/sites-enabled/
sudo nginx -t
sudo systemctl reload nginx

# Obtenir le certificat SSL
sudo certbot --nginx -d votre-domaine.com
```

## 📊 Monitoring

### Actuator Endpoints

L'application expose des endpoints de monitoring via Spring Actuator :

- **Health Check** : `http://localhost:8997/actuator/health`
- **Info** : `http://localhost:8997/actuator/info`

### Configuration du Monitoring

Dans `application-prod.properties` :

```properties
# Actuator
management.endpoints.web.exposure.include=health,info,metrics
management.endpoint.health.show-details=when-authorized
management.metrics.export.prometheus.enabled=true
```

## 🔄 Backup Automatique

Le système de backup est configuré pour s'exécuter automatiquement tous les jours à 2h du matin.

### Configuration

Dans `application-prod.properties` :

```properties
# Backup
app.backup.enabled=true
app.backup.directory=/var/backups/rapidclean
app.backup.retention-days=30
```

### Backup Manuel

Via l'interface admin : `/admin/backup` ou via l'API :

```bash
curl -X POST http://localhost:8997/admin/backup/create \
  -H "Cookie: JSESSIONID=..."
```

## 🔐 Sécurité

### Checklist de Sécurité

- [x] Rate limiting activé (protection brute force)
- [x] Headers de sécurité configurés
- [x] CSRF protection activée
- [x] Validation des entrées
- [x] Mots de passe cryptés (BCrypt)
- [ ] HTTPS obligatoire (à configurer avec Nginx)
- [ ] Firewall configuré
- [ ] Logs de sécurité activés

### Configuration du Firewall

```bash
# Ubuntu/Debian avec UFW
sudo ufw allow 22/tcp    # SSH
sudo ufw allow 80/tcp    # HTTP
sudo ufw allow 443/tcp   # HTTPS
sudo ufw enable
```

## 📝 Logs

### Emplacement des Logs

- **Application** : `/var/log/rapidclean/` (si configuré) ou `journalctl -u rapidclean`
- **Nginx** : `/var/log/nginx/`
- **PostgreSQL** : `/var/log/postgresql/`

### Rotation des Logs

Configurez `logrotate` pour `/var/log/rapidclean/` :

```bash
sudo nano /etc/logrotate.d/rapidclean
```

```conf
/var/log/rapidclean/*.log {
    daily
    rotate 30
    compress
    delaycompress
    missingok
    notifempty
    create 0640 rapidclean rapidclean
}
```

## 🧪 Tests Post-Déploiement

### 1. Vérifier que l'Application Démarre

```bash
curl http://localhost:8997/actuator/health
```

Réponse attendue : `{"status":"UP"}`

### 2. Tester l'Authentification

```bash
# Tester la connexion admin
curl -X POST http://localhost:8997/admin-secret-access \
  -d "username=admin@bmsrapidclean.com&password=admin123"
```

### 3. Vérifier les Backups

```bash
ls -lh /var/backups/rapidclean/
```

## 🔄 Mise à Jour

### Processus de Mise à Jour

1. **Arrêter le service**
   ```bash
   sudo systemctl stop rapidclean
   ```

2. **Backup de la base de données**
   ```bash
   sudo -u rapidclean /opt/rapidclean/backup.sh
   ```

3. **Mettre à jour le JAR**
   ```bash
   sudo cp target/rapid-clean-0.0.1-SNAPSHOT.jar /opt/rapidclean/
   ```

4. **Redémarrer le service**
   ```bash
   sudo systemctl start rapidclean
   ```

5. **Vérifier les logs**
   ```bash
   sudo journalctl -u rapidclean -f
   ```

## 🆘 Dépannage

### L'application ne démarre pas

```bash
# Vérifier les logs
sudo journalctl -u rapidclean -n 100

# Vérifier la configuration
sudo systemctl status rapidclean

# Vérifier les ports
sudo netstat -tlnp | grep 8997
```

### Problèmes de Base de Données

```bash
# Vérifier la connexion PostgreSQL
sudo -u postgres psql -d rapidclean -c "SELECT 1;"

# Vérifier les logs PostgreSQL
sudo tail -f /var/log/postgresql/postgresql-*.log
```

### Problèmes de Permissions

```bash
# Vérifier les permissions
ls -la /opt/rapidclean/
ls -la /var/backups/rapidclean/
ls -la /var/uploads/rapidclean/
```

## 📞 Support

En cas de problème, vérifier :
1. Les logs de l'application
2. Les logs de Nginx
3. Les logs de PostgreSQL
4. La configuration des variables d'environnement
5. Les permissions des fichiers et répertoires



