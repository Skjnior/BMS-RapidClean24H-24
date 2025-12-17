# Tests - BMS RapidClean

## Structure des Tests

```
src/test/java/com/rapidclean/
├── service/          # Tests unitaires des services
├── controller/       # Tests d'intégration des contrôleurs
├── repository/       # Tests des repositories
└── security/         # Tests de sécurité
```

## Exécution des Tests

### Tous les tests
```bash
mvn test
```

### Tests spécifiques
```bash
mvn test -Dtest=AuditServiceTest
mvn test -Dtest=FileStorageServiceTest
mvn test -Dtest=AdminControllerTest
```

### Avec couverture de code
```bash
mvn test jacoco:report
```

## Tests Implémentés

### Service Tests
- ✅ `AuditServiceTest` - Tests du service d'audit
- ✅ `FileStorageServiceTest` - Tests du service de stockage de fichiers

### Controller Tests
- ✅ `AdminControllerTest` - Tests d'intégration du contrôleur admin

## Tests à Ajouter

- [ ] Tests pour `UserDetailsService`
- [ ] Tests pour `NotificationService`
- [ ] Tests pour `EmployeeController`
- [ ] Tests pour `ClientController`
- [ ] Tests de sécurité (authentification, autorisation)
- [ ] Tests de validation des entités

## Configuration

Les tests utilisent :
- **JUnit 5** pour les tests unitaires
- **Mockito** pour les mocks
- **Spring Test** pour les tests d'intégration
- **Spring Security Test** pour les tests de sécurité

## Notes

- Les tests utilisent des mocks pour éviter les dépendances à la base de données
- Les tests d'intégration nécessitent une configuration de test séparée
- Les tests de sécurité utilisent `@WithMockUser` pour simuler l'authentification



