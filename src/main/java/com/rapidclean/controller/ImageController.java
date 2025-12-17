package com.rapidclean.controller;

import com.rapidclean.service.FileStorageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

@Controller
public class ImageController {

    @Autowired
    private FileStorageService fileStorageService;

    @GetMapping("/images/observations/{filename:.+}")
    public ResponseEntity<Resource> getObservationImage(@PathVariable String filename) {
        try {
            System.out.println("ImageController: Tentative de chargement de l'image: " + filename);
            
            // Le filename peut déjà être encodé, on essaie de le décoder
            String decodedFilename = filename;
            try {
                decodedFilename = java.net.URLDecoder.decode(filename, StandardCharsets.UTF_8.toString());
            } catch (Exception e) {
                // Si le décodage échoue, on utilise le filename tel quel
                System.out.println("ImageController: Décodage non nécessaire pour: " + filename);
            }
            
            System.out.println("ImageController: Filename décodé: " + decodedFilename);
            
            // Construire le chemin complet
            String filePath = "observations/" + decodedFilename;
            System.out.println("ImageController: Chemin complet: " + filePath);
            
            Resource resource = fileStorageService.loadFileAsResource(filePath);
            
            if (resource == null) {
                System.out.println("ImageController: Resource est null pour: " + filePath);
                return ResponseEntity.notFound().build();
            }
            
            if (!resource.exists()) {
                System.out.println("ImageController: Resource n'existe pas pour: " + filePath);
                return ResponseEntity.notFound().build();
            }
            
            System.out.println("ImageController: Image trouvée: " + resource.getFilename());

            // Déterminer le type de contenu
            String contentType = "image/jpeg";
            String lowerFilename = decodedFilename.toLowerCase();
            if (lowerFilename.endsWith(".png")) {
                contentType = "image/png";
            } else if (lowerFilename.endsWith(".gif")) {
                contentType = "image/gif";
            } else if (lowerFilename.endsWith(".webp")) {
                contentType = "image/webp";
            }

            return ResponseEntity.ok()
                    .contentType(MediaType.parseMediaType(contentType))
                    .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + 
                            URLEncoder.encode(decodedFilename, StandardCharsets.UTF_8) + "\"")
                    .body(resource);
        } catch (Exception e) {
            System.err.println("ImageController: Erreur lors du chargement de l'image: " + filename);
            e.printStackTrace();
            return ResponseEntity.notFound().build();
        }
    }
}

