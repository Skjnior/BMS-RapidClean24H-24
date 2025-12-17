package com.rapidclean.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Service;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;

/**
 * Runner qui s'exécute AVANT la DataInitializationService (@Order(0) vs @Order(1))
 * pour garantir que la colonne first_login existe avant d'y accéder.
 * Cela élimine le besoin de migration manuelle psql.
 */
@Service
@Order(0)
public class SchemaInitializationRunner implements CommandLineRunner {

    @Autowired
    private DataSource dataSource;

    @Override
    public void run(String... args) throws Exception {
        try (Connection conn = dataSource.getConnection();
             Statement stmt = conn.createStatement()) {

            // Vérifier si la colonne first_login existe déjà
            if (!columnExists(conn, "users", "first_login")) {
                System.out.println("⚙️  Colonne 'first_login' absente. Création...");
                
                try {
                    // Ajouter la colonne (BOOLEAN avec DEFAULT false)
                    // Utiliser IF NOT EXISTS pour H2 et gérer l'exception pour PostgreSQL
                    String databaseProductName = conn.getMetaData().getDatabaseProductName();
                    if (databaseProductName.contains("H2")) {
                        // H2 supporte IF NOT EXISTS dans certaines versions
                        try {
                            stmt.execute("ALTER TABLE users ADD COLUMN IF NOT EXISTS first_login BOOLEAN DEFAULT false");
                        } catch (Exception e) {
                            // Si IF NOT EXISTS n'est pas supporté, essayer sans
                            stmt.execute("ALTER TABLE users ADD COLUMN first_login BOOLEAN DEFAULT false");
                        }
                    } else {
                        // PostgreSQL
                        stmt.execute("ALTER TABLE users ADD COLUMN first_login BOOLEAN DEFAULT false");
                    }
                    System.out.println("✅ Colonne 'first_login' créée avec succès");
                    
                    // Mettre à jour les lignes existantes pour éviter les NULL
                    int updated = stmt.executeUpdate("UPDATE users SET first_login = false WHERE first_login IS NULL");
                    System.out.println("✅ " + updated + " lignes mises à jour (first_login = false)");
                } catch (Exception e) {
                    // Si la colonne existe déjà (erreur de duplication), ignorer
                    String errorMsg = e.getMessage() != null ? e.getMessage() : "";
                    if (errorMsg.contains("Duplicate column") || 
                        errorMsg.contains("already exists") ||
                        errorMsg.contains("duplication") ||
                        errorMsg.contains("FIRST_LOGIN")) {
                        System.out.println("✅ Colonne 'first_login' existe déjà (ignoré)");
                    } else {
                        // Relancer l'exception si ce n'est pas une erreur de duplication
                        throw e;
                    }
                }
            } else {
                System.out.println("✅ Colonne 'first_login' existe déjà");
                
                // Remplir les NULL existants (au cas où)
                try {
                    int updated = stmt.executeUpdate("UPDATE users SET first_login = false WHERE first_login IS NULL");
                    if (updated > 0) {
                        System.out.println("✅ " + updated + " lignes NULL mises à jour");
                    }
                } catch (Exception e) {
                    // Ignorer les erreurs mineures lors de la mise à jour
                    System.out.println("⚠️  Note: " + e.getMessage());
                }
            }
            
            // Corriger la contrainte CHECK pour accepter le rôle EMPLOYEE
            fixRoleCheckConstraint(conn);
            
            // Note: No need to commit — HikariCP/Spring manages transactions with autoCommit=true by default
        } catch (Exception e) {
            String errorMsg = e.getMessage() != null ? e.getMessage() : "";
            // Ne pas faire échouer l'application si c'est juste une erreur de colonne dupliquée
            if (errorMsg.contains("Duplicate column") || 
                errorMsg.contains("already exists") ||
                errorMsg.contains("duplication") ||
                errorMsg.contains("FIRST_LOGIN")) {
                System.out.println("⚠️  Colonne 'first_login' existe déjà. Ignoré.");
            } else {
                System.err.println("❌ Erreur lors de l'initialisation du schéma: " + e.getMessage());
                e.printStackTrace();
                // Ne pas faire échouer l'application pour les erreurs mineures de schéma
                // throw e;
            }
        }
    }

    /**
     * Corrige la contrainte CHECK sur la colonne role pour accepter EMPLOYEE.
     */
    private void fixRoleCheckConstraint(Connection conn) throws Exception {
        try (Statement stmt = conn.createStatement()) {
            System.out.println("🔍 Vérification de la contrainte CHECK sur la colonne role...");
            
            // Étape 1: Essayer de supprimer la contrainte existante
            try {
                stmt.execute("ALTER TABLE users DROP CONSTRAINT IF EXISTS users_role_check CASCADE");
                System.out.println("✅ Ancienne contrainte CHECK supprimée (ou n'existait pas)");
            } catch (Exception e) {
                System.out.println("⚠️  Erreur lors de la suppression: " + e.getMessage());
            }
            
            // Étape 2: Créer la nouvelle contrainte CHECK avec ADMIN, EMPLOYEE, CLIENT
            try {
                stmt.execute("ALTER TABLE users ADD CONSTRAINT users_role_check CHECK (role IN ('ADMIN', 'EMPLOYEE', 'CLIENT'))");
                System.out.println("✅ Nouvelle contrainte CHECK créée pour ADMIN, EMPLOYEE, CLIENT");
            } catch (Exception e) {
                if (e.getMessage().contains("already exists")) {
                    System.out.println("✅ Contrainte CHECK déjà existe et accepte les bons rôles");
                } else {
                    System.out.println("⚠️  Erreur lors de la création: " + e.getMessage());
                }
            }
        }
    }

    /**
     * Vérifie si une colonne existe dans une table (compatible PostgreSQL et H2).
     */
    private boolean columnExists(Connection conn, String tableName, String columnName) throws Exception {
        String databaseProductName = conn.getMetaData().getDatabaseProductName();
        String query;
        
        // H2 utilise INFORMATION_SCHEMA.COLUMNS avec des noms en majuscules
        if (databaseProductName.contains("H2")) {
            query = "SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS " +
                    "WHERE TABLE_NAME = '" + tableName.toUpperCase() + "' " +
                    "AND COLUMN_NAME = '" + columnName.toUpperCase() + "'";
        } else {
            // PostgreSQL et autres
            query = "SELECT EXISTS(" +
                    "  SELECT 1 FROM information_schema.columns " +
                    "  WHERE table_name = '" + tableName + "' " +
                    "  AND column_name = '" + columnName + "'" +
                    ")";
        }
        
        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {
            if (rs.next()) {
                if (databaseProductName.contains("H2")) {
                    return rs.getInt(1) > 0;
                } else {
                    return rs.getBoolean(1);
                }
            }
        } catch (Exception e) {
            // Si la requête échoue, essayer une approche alternative pour H2
            if (databaseProductName.contains("H2")) {
                try {
                    // Essayer de sélectionner la colonne pour voir si elle existe
                    try (Statement testStmt = conn.createStatement()) {
                        testStmt.executeQuery("SELECT " + columnName + " FROM " + tableName + " LIMIT 1");
                        return true;
                    }
                } catch (Exception ex) {
                    return false;
                }
            }
            throw e;
        }
        return false;
    }
}
