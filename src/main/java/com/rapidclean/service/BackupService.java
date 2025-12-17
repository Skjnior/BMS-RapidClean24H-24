package com.rapidclean.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

/**
 * Service pour gérer les backups automatiques de la base de données
 */
@Service
public class BackupService {

    private static final Logger logger = LoggerFactory.getLogger(BackupService.class);
    
    @Value("${spring.datasource.url:jdbc:postgresql://localhost:5432/rapidclean}")
    private String databaseUrl;
    
    @Value("${spring.datasource.username:postgres}")
    private String databaseUsername;
    
    @Value("${spring.datasource.password:}")
    private String databasePassword;
    
    @Value("${app.backup.directory:./backups}")
    private String backupDirectory;
    
    @Value("${app.backup.enabled:true}")
    private boolean backupEnabled;
    
    @Value("${app.backup.retention-days:30}")
    private int retentionDays;

    /**
     * Effectue un backup de la base de données PostgreSQL
     */
    public boolean performBackup() {
        if (!backupEnabled) {
            logger.info("Backup désactivé dans la configuration");
            return false;
        }
        
        try {
            // Extraire le nom de la base de données depuis l'URL
            String dbName = extractDatabaseName(databaseUrl);
            if (dbName == null) {
                logger.error("Impossible d'extraire le nom de la base de données depuis l'URL: {}", databaseUrl);
                return false;
            }
            
            // Créer le répertoire de backup s'il n'existe pas
            Path backupPath = Paths.get(backupDirectory);
            if (!Files.exists(backupPath)) {
                Files.createDirectories(backupPath);
                logger.info("Répertoire de backup créé: {}", backupDirectory);
            }
            
            // Générer le nom du fichier de backup
            String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd_HH-mm-ss"));
            String backupFileName = String.format("rapidclean_backup_%s.sql", timestamp);
            Path backupFile = backupPath.resolve(backupFileName);
            
            // Construire la commande pg_dump
            List<String> command = new ArrayList<>();
            command.add("pg_dump");
            command.add("-h");
            command.add(extractHost(databaseUrl));
            command.add("-p");
            command.add(extractPort(databaseUrl));
            command.add("-U");
            command.add(databaseUsername);
            command.add("-d");
            command.add(dbName);
            command.add("-F");
            command.add("p"); // Format plain text (SQL)
            command.add("--no-owner"); // Ne pas inclure les commandes de propriétaire
            command.add("--no-acl"); // Ne pas inclure les commandes de permissions
            command.add("--clean"); // Inclure les commandes DROP
            command.add("--if-exists"); // Utiliser IF EXISTS pour les DROP
            command.add("--verbose"); // Mode verbeux pour le débogage
            
            logger.info("Exécution de la commande: {}", String.join(" ", command));
            logger.info("Base de données: {}, Host: {}, Port: {}, User: {}", 
                       dbName, extractHost(databaseUrl), extractPort(databaseUrl), databaseUsername);
            
            // Exécuter la commande et rediriger la sortie vers le fichier
            ProcessBuilder processBuilder = new ProcessBuilder(command);
            processBuilder.environment().put("PGPASSWORD", databasePassword);
            
            // Rediriger la sortie standard vers le fichier
            processBuilder.redirectOutput(backupFile.toFile());
            // Rediriger les erreurs vers un fichier temporaire pour les capturer
            File errorFile = File.createTempFile("pg_dump_error_", ".log");
            processBuilder.redirectError(errorFile);
            
            Process process = processBuilder.start();
            int exitCode = process.waitFor();
            
            // Lire les erreurs si elles existent
            StringBuilder errorOutput = new StringBuilder();
            if (errorFile.exists() && errorFile.length() > 0) {
                try (BufferedReader reader = new BufferedReader(
                        new InputStreamReader(new FileInputStream(errorFile)))) {
                    String line;
                    while ((line = reader.readLine()) != null) {
                        errorOutput.append(line).append("\n");
                        logger.warn("pg_dump stderr: {}", line);
                    }
                }
            }
            // Supprimer le fichier d'erreur temporaire
            errorFile.delete();
            
            // Vérifier que le fichier a été créé et n'est pas vide
            if (exitCode == 0) {
                if (!Files.exists(backupFile)) {
                    logger.error("Le fichier de backup n'a pas été créé: {}", backupFile);
                    return false;
                }
                
                long fileSize = Files.size(backupFile);
                if (fileSize == 0) {
                    logger.error("Le fichier de backup est vide (0 octets). Erreurs possibles: {}", errorOutput.toString());
                    Files.deleteIfExists(backupFile);
                    return false;
                }
                
                logger.info("Backup réussi: {} ({} octets)", backupFile, fileSize);
                
                // Nettoyer les anciens backups
                cleanOldBackups();
                
                return true;
            } else {
                logger.error("Échec du backup. Code de sortie: {}. Erreurs: {}", exitCode, errorOutput.toString());
                // Supprimer le fichier vide en cas d'erreur
                Files.deleteIfExists(backupFile);
                return false;
            }
            
        } catch (Exception e) {
            logger.error("Erreur lors du backup de la base de données", e);
            return false;
        }
    }
    
    /**
     * Backup automatique quotidien à 2h du matin
     */
    @Scheduled(cron = "0 0 2 * * ?")
    public void scheduledBackup() {
        logger.info("Démarrage du backup automatique programmé");
        performBackup();
    }
    
    /**
     * Nettoie les anciens backups selon la politique de rétention
     */
    private void cleanOldBackups() {
        try {
            Path backupPath = Paths.get(backupDirectory);
            if (!Files.exists(backupPath)) {
                return;
            }
            
            LocalDateTime cutoffDate = LocalDateTime.now().minusDays(retentionDays);
            long cutoffTimestamp = cutoffDate.atZone(java.time.ZoneId.systemDefault()).toInstant().toEpochMilli();
            
            Files.list(backupPath)
                .filter(Files::isRegularFile)
                .filter(path -> path.toString().endsWith(".sql"))
                .filter(path -> {
                    try {
                        return Files.getLastModifiedTime(path).toMillis() < cutoffTimestamp;
                    } catch (IOException e) {
                        return false;
                    }
                })
                .forEach(path -> {
                    try {
                        Files.delete(path);
                        logger.info("Ancien backup supprimé: {}", path.getFileName());
                    } catch (IOException e) {
                        logger.warn("Impossible de supprimer l'ancien backup: {}", path, e);
                    }
                });
                
        } catch (IOException e) {
            logger.error("Erreur lors du nettoyage des anciens backups", e);
        }
    }
    
    private String extractDatabaseName(String url) {
        // Format: jdbc:postgresql://host:port/database
        if (url == null || !url.contains("/")) {
            return null;
        }
        String[] parts = url.split("/");
        if (parts.length > 0) {
            String lastPart = parts[parts.length - 1];
            // Enlever les paramètres de requête s'il y en a
            if (lastPart.contains("?")) {
                lastPart = lastPart.split("\\?")[0];
            }
            return lastPart;
        }
        return null;
    }
    
    private String extractHost(String url) {
        // Format: jdbc:postgresql://host:port/database
        if (url == null || !url.contains("://")) {
            return "localhost";
        }
        String hostPort = url.split("://")[1].split("/")[0];
        if (hostPort.contains(":")) {
            return hostPort.split(":")[0];
        }
        return hostPort;
    }
    
    private String extractPort(String url) {
        // Format: jdbc:postgresql://host:port/database
        if (url == null || !url.contains("://")) {
            return "5432";
        }
        String hostPort = url.split("://")[1].split("/")[0];
        if (hostPort.contains(":")) {
            return hostPort.split(":")[1];
        }
        return "5432";
    }
    
    /**
     * Liste les backups disponibles
     */
    public List<String> listBackups() {
        List<String> backups = new ArrayList<>();
        try {
            Path backupPath = Paths.get(backupDirectory);
            if (Files.exists(backupPath)) {
                Files.list(backupPath)
                    .filter(Files::isRegularFile)
                    .filter(path -> path.toString().endsWith(".sql"))
                    .sorted((p1, p2) -> {
                        try {
                            return Long.compare(
                                Files.getLastModifiedTime(p2).toMillis(),
                                Files.getLastModifiedTime(p1).toMillis()
                            );
                        } catch (IOException e) {
                            return 0;
                        }
                    })
                    .forEach(path -> backups.add(path.getFileName().toString()));
            }
        } catch (IOException e) {
            logger.error("Erreur lors de la liste des backups", e);
        }
        return backups;
    }
}

