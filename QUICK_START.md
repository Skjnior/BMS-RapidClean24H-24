# 🚀 Quick Start - Système de Gestion Employés

## ⚡ En 5 Minutes

### 1. **Démarrer l'Application**
```bash
cd C:\Users\Official\Desktop\DEV\rapidClean
mvn -DskipTests spring-boot:run
```

**Attendez** jusqu'à voir:
```
Tomcat started on port 8997 (http) with context path ''
✅ Colonne 'first_login' existe déjà
✅ Nouvelle contrainte CHECK créée pour ADMIN, EMPLOYEE, CLIENT
```

### 2. **Créer un Employé (Admin)**
1. Ouvrez: `http://localhost:8997/admin/employees`
2. Login: `admin@bmsrapidclean.com` / `admin123`
3. Cliquez **"➕ Créer un Employé"**
4. Remplissez:
   ```
   Prénom: Mamadou
   Nom: Ly
   Email: mamadou.ly@example.com
   Téléphone: +1234567890
   Rôle: EMPLOYEE (automatique)
   Statut: Actif
   ```
5. Cliquez **"Enregistrer"** ✅

### 3. **Première Connexion Employé**
1. Ouvrez: `http://localhost:8997/employee-login`
2. Email: `mamadou.ly@example.com`
3. Mot de passe: `temp123`
4. Cliquez **"Se connecter"** ✅

### 4. **Changement de Mot de Passe Obligatoire**
1. **Redirection automatique** vers: `/employee/change-password`
2. Nouveau mot de passe: `MyNewPass123` (minimum 6 caractères)
3. Confirmation: `MyNewPass123`
4. Cliquez **"Modifier et continuer"** ✅
5. **Déconnexion automatique** 🔄

### 5. **Reconnexion avec Nouveau Mot de Passe**
1. Ouvrez: `http://localhost:8997/employee-login`
2. Email: `mamadou.ly@example.com`
3. Mot de passe: `MyNewPass123` (nouveau)
4. Cliquez **"Se connecter"** ✅

### 6. **Accès au Dashboard Employé**
1. **Redirection vers**: `http://localhost:8997/employee/dashboard`
2. Voyez le menu avec:
   - 📊 Pointage des Heures
   - 📅 Déclaration d'Absence
   - 🔍 Observations au Lieu de Travail
   - 👤 Mon Profil

---

## 📊 Tester les Fonctionnalités

### A. **Pointage des Heures** ⏰
1. Cliquez **"📊 Pointage des Heures"**
2. Cliquez **"✅ Enregistrer Arrivée"** (fixe l'heure actuelle)
3. Plus tard, cliquez **"🚪 Enregistrer Départ"** (fixe l'heure actuelle)
4. Voyez l'historique avec temps travaillé

### B. **Déclaration d'Absence** 📅
1. Cliquez **"📅 Déclaration d'Absence"**
2. Sélectionnez une **date d'absence**
3. Choisissez un **type**:
   - Congé payé
   - Arrêt maladie
   - Congé sans solde
   - Absence justifiée
4. Ajoutez un **motif**: (ex: "Rendez-vous médical")
5. Cliquez **"Déclarer l'Absence"** ✅
6. Voyez l'historique des absences

### C. **Observations au Lieu de Travail** 🔍
1. Cliquez **"🔍 Observations"**
2. **Titre**: (ex: "Climatisation cassée")
3. **Description**: (ex: "La climatisation du bureau ne fonctionne plus depuis hier")
4. **Priorité**: 
   - 🟢 BASSE (amélioration cosmétique)
   - 🟡 MOYENNE (inconvénient mineur)
   - 🟠 HAUTE (affecte la productivité)
   - 🔴 CRITIQUE (défaut de sécurité)
5. Cliquez **"Soumettre le Signalement"** ✅
6. Voyez l'historique des observations avec horodatage

---

## 🔐 Identifiants de Test

### Admin
```
Email: admin@bmsrapidclean.com
Mot de passe: admin123
URL: http://localhost:8997/admin/employees
```

### Employé Créé
```
Email: mamadou.ly@example.com
Mot de passe initial: temp123
Nouveau mot de passe: MyNewPass123 (après première connexion)
URL: http://localhost:8997/employee-login
```

---

## 🧭 URLs Principales

| Route | Rôle | Objectif |
|-------|------|----------|
| `/employee-login` | Tous | Connexion employé |
| `/admin/employees` | Admin | Gestion des employés |
| `/admin/employees/new` | Admin | Créer employé |
| `/employee/dashboard` | Employé | Tableau de bord |
| `/employee/time-tracking` | Employé | Pointage heures |
| `/employee/absences` | Employé | Absence justifiée |
| `/employee/observations` | Employé | Signaler problème |
| `/logout` | Tous | Déconnexion |

---

## ✅ Checklist de Vérification

Après démarrage, vérifiez:

- [ ] Admin peut accéder à `/admin/employees`
- [ ] Admin peut créer un nouvel employé
- [ ] Le mot de passe `temp123` est assigné automatiquement
- [ ] Employé peut se connecter à `/employee-login`
- [ ] Employé est forcé vers `/employee/change-password`
- [ ] Employé peut changer son mot de passe
- [ ] Après changement: **auto-logout**
- [ ] Employé peut se reconnecter avec nouveau mot de passe
- [ ] Employé accède à `/employee/dashboard`
- [ ] Pointage des heures fonctionne
- [ ] Déclaration d'absence fonctionne
- [ ] Signalement observation fonctionne
- [ ] Historique affiche les entrées correctement

---

## 🐛 Dépannage Rapide

### Problème: Colonne `first_login` manquante
```
Solution: C'est normal au premier démarrage.
         SchemaInitializationRunner la crée automatiquement.
         Redémarrez l'application.
```

### Problème: Erreur "constraint_violation" au login
```
Solution: La contrainte CHECK n'accepte pas EMPLOYEE.
         Attendez que SchemaInitializationRunner la corrige.
         Redémarrez l'application.
```

### Problème: Employé ne peut pas créer
```
Solution: Vérifiez que l'email n'existe pas déjà.
         Les emails doivent être uniques en BD.
```

### Problème: Pas de redirection vers change-password
```
Solution: Vérifiez que WebConfig enregistre l'interceptor.
         Attendez quelques secondes après login.
         Redémarrez l'application.
```

### Problème: `/logout` ne fonctionne pas
```
Solution: C'est l'endpoint standard Spring Security.
         Si absent, vérifiez SecurityConfig.
         Utilisez plutôt:
         <form action="/logout" method="POST">
```

---

## 💡 Tips Utiles

✅ **Mot de passe fort recommandé**: 
   - Minimum 6 caractères
   - Mélanger majuscules/minuscules/chiffres/symboles

✅ **Tests multiples employés**:
   - Créez plusieurs employés avec emails différents
   - Chacun a son historique privé
   - Les données ne se chevauchent pas

✅ **Historique des actions**:
   - Pointages: sauvegardés par date
   - Absences: avec motif et type
   - Observations: avec horodatage et priorité

✅ **Logs utiles** en console:
   - Cherchez `✅ Colonne 'first_login'`
   - Cherchez `✅ Nouvelle contrainte CHECK`
   - Cherchez `Enregistrer Arrivée enregistrée`

---

## 📚 Documentation Complète

Pour plus de détails, consultez:
- [`EMPLOYEE_SYSTEM_COMPLETE.md`](./EMPLOYEE_SYSTEM_COMPLETE.md) - Documentation complète
- [`CHANGES_SUMMARY.md`](./CHANGES_SUMMARY.md) - Résumé des changements
- [`EMPLOYEE_TEST_CHECKLIST.md`](./EMPLOYEE_TEST_CHECKLIST.md) - Checklist exhaustive

---

**Dernière mise à jour**: 13 Décembre 2025  
**Prêt pour**: ✅ Test et Déploiement
