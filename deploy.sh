#!/bin/bash

# BMS Rapid Clean - Script de Déploiement
echo "🚀 BMS Rapid Clean - Déploiement en Production"
echo "=============================================="

# Configuration
APP_NAME="rapid-clean"
VERSION="1.0.0"
DOCKER_IMAGE="bms-rapid-clean"
DOCKER_TAG="latest"

# Vérifier si Docker est installé
if ! command -v docker &> /dev/null; then
    echo "❌ Docker n'est pas installé. Veuillez installer Docker."
    exit 1
fi

# Vérifier si Docker Compose est installé
if ! command -v docker-compose &> /dev/null; then
    echo "❌ Docker Compose n'est pas installé. Veuillez installer Docker Compose."
    exit 1
fi

echo "✅ Docker et Docker Compose détectés"

# Arrêter les conteneurs existants
echo "🛑 Arrêt des conteneurs existants..."
docker-compose down

# Nettoyer les images anciennes
echo "🧹 Nettoyage des images anciennes..."
docker system prune -f

# Construire l'application
echo "🔨 Construction de l'application..."
mvn clean package -DskipTests

if [ $? -ne 0 ]; then
    echo "❌ Erreur lors de la construction de l'application"
    exit 1
fi

# Construire l'image Docker
echo "🐳 Construction de l'image Docker..."
docker build -t $DOCKER_IMAGE:$DOCKER_TAG .

if [ $? -ne 0 ]; then
    echo "❌ Erreur lors de la construction de l'image Docker"
    exit 1
fi

# Démarrer les services
echo "🚀 Démarrage des services..."
docker-compose up -d

# Attendre que les services soient prêts
echo "⏳ Attente du démarrage des services..."
sleep 30

# Vérifier le statut des services
echo "📊 Vérification du statut des services..."
docker-compose ps

# Vérifier la santé de l'application
echo "🏥 Vérification de la santé de l'application..."
sleep 10

# Test de connectivité
if curl -f http://localhost:8080/actuator/health > /dev/null 2>&1; then
    echo "✅ Application démarrée avec succès"
    echo ""
    echo "🌐 L'application est accessible sur:"
    echo "   - Application: http://localhost:8080"
    echo "   - Admin: http://localhost:8080/admin/dashboard"
    echo "   - Client: http://localhost:8080/client/dashboard"
    echo ""
    echo "📊 Comptes par défaut:"
    echo "   - Admin: admin@bmsrapidclean.com / admin123"
    echo "   - Client: client@example.com / client123"
    echo ""
    echo "📝 Logs de l'application:"
    echo "   docker-compose logs -f app"
    echo ""
    echo "🛑 Pour arrêter l'application:"
    echo "   docker-compose down"
else
    echo "❌ L'application n'est pas accessible"
    echo "📝 Vérifiez les logs:"
    echo "   docker-compose logs app"
    exit 1
fi

echo "🎉 Déploiement terminé avec succès!"
