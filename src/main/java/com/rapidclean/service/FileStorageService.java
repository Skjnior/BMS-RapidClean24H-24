package com.rapidclean.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
public class FileStorageService {

    @Value("${app.upload.dir:src/main/resources/static/images/observations}")
    private String uploadDir;

    // Taille maximale : 5 MB
    private static final long MAX_FILE_SIZE = 5 * 1024 * 1024;
    
    // Types MIME autorisés
    private static final List<String> ALLOWED_CONTENT_TYPES = List.of(
        "image/jpeg", "image/jpg", "image/png", "image/gif", "image/webp"
    );

    public String storeFile(MultipartFile file) throws IOException {
        if (file == null || file.isEmpty()) {
            return null;
        }

        // Valider la taille du fichier
        if (file.getSize() > MAX_FILE_SIZE) {
            throw new IllegalArgumentException(
                "Le fichier est trop volumineux. Taille maximale autorisée : " + 
                (MAX_FILE_SIZE / 1024 / 1024) + " MB"
            );
        }

        // Créer le répertoire s'il n'existe pas
        Path uploadPath = Paths.get(uploadDir);
        if (!Files.exists(uploadPath)) {
            Files.createDirectories(uploadPath);
        }

        // Générer un nom de fichier unique
        String originalFilename = file.getOriginalFilename();
        String extension = "";
        if (originalFilename != null && originalFilename.contains(".")) {
            extension = originalFilename.substring(originalFilename.lastIndexOf("."));
        }
        String filename = UUID.randomUUID().toString() + extension;

        // Valider le type de fichier
        String contentType = file.getContentType();
        if (contentType == null || !ALLOWED_CONTENT_TYPES.contains(contentType.toLowerCase())) {
            throw new IllegalArgumentException(
                "Type de fichier non autorisé. Types acceptés : JPEG, PNG, GIF, WEBP"
            );
        }

        // Valider l'extension du fichier
        String lowerExtension = extension.toLowerCase();
        if (!lowerExtension.matches("\\.(jpg|jpeg|png|gif|webp)")) {
            throw new IllegalArgumentException(
                "Extension de fichier non autorisée. Extensions acceptées : .jpg, .jpeg, .png, .gif, .webp"
            );
        }

        // Sauvegarder le fichier
        Path filePath = uploadPath.resolve(filename);
        Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);

        // Retourner le chemin relatif pour l'URL
        return "observations/" + filename;
    }
    
    public String storeServiceImage(MultipartFile file) throws IOException {
        if (file == null || file.isEmpty()) {
            return null;
        }

        // Déterminer le répertoire pour les images de services
        String servicesDir = uploadDir.replace("observations", "services");
        Path uploadPath = Paths.get(servicesDir);
        if (!Files.exists(uploadPath)) {
            Files.createDirectories(uploadPath);
        }

        // Générer un nom de fichier unique
        String originalFilename = file.getOriginalFilename();
        String extension = "";
        if (originalFilename != null && originalFilename.contains(".")) {
            extension = originalFilename.substring(originalFilename.lastIndexOf("."));
        }
        String filename = UUID.randomUUID().toString() + extension;

        // Vérifier que c'est une image
        String contentType = file.getContentType();
        if (contentType == null || !contentType.startsWith("image/")) {
            throw new IllegalArgumentException("Le fichier doit être une image");
        }

        // Sauvegarder le fichier
        Path filePath = uploadPath.resolve(filename);
        Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);

        // Retourner seulement le nom du fichier (sans le chemin)
        return filename;
    }
    
    public List<String> storeFiles(MultipartFile[] files) throws IOException {
        List<String> storedPaths = new ArrayList<>();
        if (files == null || files.length == 0) {
            return storedPaths;
        }
        
        for (MultipartFile file : files) {
            if (file != null && !file.isEmpty()) {
                String path = storeFile(file);
                if (path != null) {
                    storedPaths.add(path);
                }
            }
        }
        
        return storedPaths;
    }

    public boolean deleteFile(String filePath) {
        try {
            if (filePath == null || filePath.isEmpty()) {
                return false;
            }
            
            // Si le chemin commence par "observations/", on le retire
            String relativePath = filePath.startsWith("observations/") 
                ? filePath 
                : "observations/" + filePath;
            
            Path path = Paths.get(uploadDir).resolve(relativePath.replace("observations/", ""));
            return Files.deleteIfExists(path);
        } catch (IOException e) {
            return false;
        }
    }

    public Path getFilePath(String filePath) {
        if (filePath == null || filePath.isEmpty()) {
            return null;
        }
        
        String relativePath = filePath.startsWith("observations/") 
            ? filePath.replace("observations/", "")
            : filePath;
            
        return Paths.get(uploadDir).resolve(relativePath);
    }

    public Resource loadFileAsResource(String filePath) throws MalformedURLException {
        System.out.println("FileStorageService: Chargement de la ressource: " + filePath);
        System.out.println("FileStorageService: Répertoire d'upload: " + uploadDir);
        
        Path path = getFilePath(filePath);
        if (path == null) {
            System.out.println("FileStorageService: getFilePath a retourné null");
            return null;
        }
        
        System.out.println("FileStorageService: Chemin résolu: " + path.toString());
        
        if (!Files.exists(path)) {
            System.out.println("FileStorageService: Le fichier n'existe pas: " + path.toString());
            return null;
        }
        
        try {
            Resource resource = new UrlResource(path.toUri());
            if (resource.exists() && resource.isReadable()) {
                System.out.println("FileStorageService: Ressource trouvée et lisible: " + resource.getFilename());
                return resource;
            } else {
                System.out.println("FileStorageService: Ressource non trouvée ou non lisible");
            }
        } catch (MalformedURLException e) {
            System.err.println("FileStorageService: Erreur URL: " + e.getMessage());
            e.printStackTrace();
        }
        
        return null;
    }
}

