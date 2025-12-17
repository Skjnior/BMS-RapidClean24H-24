psql -h localhost -p 5432 -U <DB_USER> -d rapidClean -c "ALTER TABLE IF NOT EXISTS users ADD COLUMN IF NOT EXISTS first_login BOOLEAN;"
psql -h localhost -p 5432 -U <DB_USER> -d rapidClean -c "UPDATE users SET first_login = false WHERE first_login IS NULL;"
psql -h localhost -p 5432 -U <DB_USER> -d rapidClean -c "ALTER TABLE users ALTER COLUMN first_login SET DEFAULT false;"
psql -h localhost -p 5432 -U <DB_USER> -d rapidClean -c "ALTER TABLE users ALTER COLUMN first_login SET NOT NULL;"
psql -h localhost -p 5432 -U <DB_USER> -d rapidClean -c "SELECT column_name,is_nullable,column_default FROM information_schema.columns WHERE table_name='users' AND column_name='first_login';"# 🔧 Guide de Dépannage - Problème de Connexion Admin

## ❌ Problème
Impossible de se connecter avec les identifiants admin :
- **Email** : admin@bmsrapidclean.com
- **Mot de passe** : admin123
- **Erreur** : "Email ou mot de passe incorrect"

---

## 🔍 Diagnostic

### Étape 1 : Vérifier l'État de l'Admin

Accédez à l'endpoint de diagnostic :
```
GET http://localhost:8997/api/debug/admin-status
```

**Réponse attendue :**
```json
{
  "status": "FOUND",
  "email": "admin@bmsrapidclean.com",
  "firstName": "Admin",
  "lastName": "System",
  "role": "ADMIN",
  "enabled": true,
  "firstLogin": false,
  "password_correct": true,
  "createdAt": "2025-12-12T10:30:00"
}
```

### Interprétation des Réponses

#### ✅ Si "password_correct": true
- Le mot de passe en base est correct
- Le problème vient ailleurs (SecurityConfig, UserDetailsService, CORS, etc.)

#### ❌ Si "password_correct": false
- Le mot de passe en base ne correspond pas
- **Solution** : Voir étape 2 ci-dessous

#### ❌ Si "status": "NOT_FOUND"
- L'admin n'existe pas en base de données
- **Solution** : Voir étape 2 ci-dessous

---

## ✅ Solutions

### Solution 1️⃣ : Réinitialisation Automatique

Exécutez cette commande pour réinitialiser automatiquement l'admin :

```bash
POST http://localhost:8997/api/debug/reset-admin
```

**Réponse attendue :**
```json
{
  "status": "RESET",
  "message": "Admin réinitialisé avec succès",
  "email": "admin@bmsrapidclean.com",
  "password": "admin123"
}
```

### Étapes après la Réinitialisation
1. ✅ Exécuter le endpoint POST `/api/debug/reset-admin`
2. ✅ Vérifier que la réponse indique "RESET" ou "CREATED"
3. ✅ Redémarrer l'application (optionnel mais recommandé)
4. ✅ Essayer de se connecter à nouveau

---

## 🔄 Redémarrage de l'Application

Après la réinitialisation, il est recommandé de redémarrer l'application pour que les changements soient pris en compte :

### Avec Maven
```bash
mvn clean spring-boot:run
```

### Ou simplement redémarrer depuis VS Code/IDE

---

## 📋 Checklist de Vérification

Après avoir réinitialisé l'admin, vérifiez :

- [ ] Endpoint diagnostic montre "password_correct": true
- [ ] Connexion à `/admin-secret-access` fonctionne
- [ ] Redirection vers `/admin/dashboard` après connexion
- [ ] Accès à la liste des employés `/admin/employees`
- [ ] Pas de message d'erreur dans la console

---

## 🛠️ Dépannage Avancé

### Si l'endpoint de réinitialisation ne fonctionne pas

#### Option A : Via la Base de Données (MySQL/H2)

```sql
-- Mettre à jour le mot de passe directement
-- Attention : le mot de passe doit être encodé en BCrypt

-- Pour H2 (développement)
DELETE FROM users WHERE email = 'admin@bmsrapidclean.com';

-- Puis redémarrer l'application pour créer un nouvel admin
```

#### Option B : Réduire le problème

1. **Vérifier les logs de l'application** pour les erreurs
   ```
   Chercher : "Utilisateur non trouvé" ou "Email ou mot de passe incorrect"
   ```

2. **Vérifier la connexion à la BD**
   ```
   Vérifier que spring.datasource.url pointe vers la bonne BD
   ```

3. **Vérifier le UserDetailsService** 
   ```
   S'assurer que UserRepository.findByEmail() fonctionne
   ```

---

## 📝 Causes Possibles de ce Problème

| Cause | Symptôme | Solution |
|-------|----------|----------|
| Mot de passe changé ou perdu | "password_correct": false | Réinitialiser via endpoint |
| Admin supprimé accidentellement | "status": "NOT_FOUND" | Réinitialiser via endpoint |
| Colonne `first_login` manquante | Erreur BD dans les logs | Exécuter migration SQL |
| BCrypt encoder mal configuré | Mot de passe jamais correct | Vérifier SecurityConfig |
| UserDetailsService cassé | Utilisateur toujours non trouvé | Vérifier UserRepository |
| Base de données corrompue | Divers | Vider et redémarrer |

---

## ✨ Prévention Futures

Pour éviter ce problème à l'avenir :

1. **Ne pas modifier DataInitializationService** sans test
2. **Toujours faire un backup** avant changements majeurs
3. **Vérifier les mots de passe** après déploiement
4. **Utiliser cet endpoint de diagnostic** régulièrement

---

## 📞 En Cas de Problème Persistant

Si le problème persiste après avoir suivi ces étapes :

1. ✅ Redémarrer complètement l'application
2. ✅ Vider le cache navigateur (Ctrl+Shift+Delete)
3. ✅ Vérifier qu'aucune autre instance de l'app n'est en cours d'exécution
4. ✅ Consulter les logs pour d'autres erreurs

```bash
# Vérifier les processus Java
# Windows
tasklist | find "java"

# Linux/Mac
ps aux | grep java
```

---

## 🎯 Résumé Rapide

**Problème** → Admin ne peut pas se connecter

**Solution Rapide** :
1. `POST /api/debug/reset-admin`
2. Redémarrer l'app
3. Essayer de se connecter

**Fin !** ✅

---

**Date de création** : 12 Décembre 2025  
**Dernière mise à jour** : 12 Décembre 2025
