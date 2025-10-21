# BMS Rapid Clean - Dockerfile
FROM openjdk:17-jdk-slim

# Informations sur l'image
LABEL maintainer="BMS Rapid Clean <info@bmsrapidclean.com>"
LABEL description="Service de nettoyage professionnel 24h/24"
LABEL version="1.0.0"

# Variables d'environnement
ENV JAVA_OPTS="-Xmx512m -Xms256m"
ENV SPRING_PROFILES_ACTIVE=prod

# Créer un utilisateur non-root
RUN groupadd -r rapidclean && useradd -r -g rapidclean rapidclean

# Créer le répertoire de travail
WORKDIR /app

# Copier les fichiers de l'application
COPY target/rapid-clean-*.jar app.jar

# Créer le répertoire pour les logs
RUN mkdir -p /app/logs && chown -R rapidclean:rapidclean /app

# Changer le propriétaire des fichiers
RUN chown -R rapidclean:rapidclean /app

# Passer à l'utilisateur non-root
USER rapidclean

# Exposer le port
EXPOSE 8080

# Point d'entrée
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]

# Santé check
HEALTHCHECK --interval=30s --timeout=3s --start-period=60s --retries=3 \
  CMD curl -f http://localhost:8080/actuator/health || exit 1
