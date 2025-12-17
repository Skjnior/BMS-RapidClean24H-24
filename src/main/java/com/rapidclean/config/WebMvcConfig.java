package com.rapidclean.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.http.CacheControl;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.nio.file.Paths;
import java.util.concurrent.TimeUnit;

@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // Chemin absolu vers le répertoire d'upload
        String uploadDir = "src/main/resources/static/images";
        String uploadPath = Paths.get(uploadDir).toFile().getAbsolutePath();
        
        // Configuration pour servir les fichiers d'observation (pas de cache pour les uploads récents)
        registry.addResourceHandler("/images/observations/**")
                .addResourceLocations("file:" + uploadPath + "/observations/")
                .setCachePeriod(0);
        
        // Configuration pour les autres images (cache 1 mois)
        registry.addResourceHandler("/images/**")
                .addResourceLocations("classpath:/static/images/")
                .setCacheControl(CacheControl.maxAge(30, TimeUnit.DAYS).cachePublic());
        
        // CSS et JS avec cache 1 an (immutable)
        registry.addResourceHandler("/css/**")
                .addResourceLocations("classpath:/static/css/")
                .setCacheControl(CacheControl.maxAge(365, TimeUnit.DAYS).cachePublic().immutable());
        
        registry.addResourceHandler("/js/**")
                .addResourceLocations("classpath:/static/js/")
                .setCacheControl(CacheControl.maxAge(365, TimeUnit.DAYS).cachePublic().immutable());
                
        // Configuration pour les autres ressources statiques (cache 7 jours)
        registry.addResourceHandler("/**")
                .addResourceLocations("classpath:/static/")
                .setCacheControl(CacheControl.maxAge(7, TimeUnit.DAYS).cachePublic());
    }
}
