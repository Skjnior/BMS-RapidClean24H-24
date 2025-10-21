#!/bin/bash

# BMS Rapid Clean - Script de Démarrage
echo "🧹 BMS Rapid Clean - Service de Nettoyage 24h/24"
echo "================================================"

# Vérifier si Java est installé
if ! command -v java &> /dev/null; then
    echo "❌ Java n'est pas installé. Veuillez installer Java 17 ou supérieur."
    exit 1
fi

# Vérifier la version de Java
JAVA_VERSION=$(java -version 2>&1 | head -n 1 | cut -d'"' -f2 | cut -d'.' -f1)
if [ "$JAVA_VERSION" -lt 17 ]; then
    echo "❌ Java 17 ou supérieur est requis. Version actuelle: $JAVA_VERSION"
    exit 1
fi

echo "✅ Java version $JAVA_VERSION détectée"

# Vérifier si Maven est installé
if ! command -v mvn &> /dev/null; then
    echo "❌ Maven n'est pas installé. Veuillez installer Maven."
    exit 1
fi

echo "✅ Maven détecté"

# Créer la base de données si elle n'existe pas
echo "📊 Configuration de la base de données..."
mysql -u root -p -e "CREATE DATABASE IF NOT EXISTS rapidclean;" 2>/dev/null || {
    echo "⚠️  Impossible de créer la base de données automatiquement."
    echo "   Veuillez créer manuellement la base de données 'rapidclean' dans MySQL."
    echo "   Commande: CREATE DATABASE rapidclean;"
}

# Compiler et lancer l'application
echo "🔨 Compilation de l'application..."
mvn clean compile

if [ $? -eq 0 ]; then
    echo "✅ Compilation réussie"
    echo "🚀 Lancement de l'application..."
    echo ""
    echo "🌐 L'application sera accessible sur: http://localhost:8080"
    echo "📧 Interface Admin: http://localhost:8080/admin/dashboard"
    echo "👤 Interface Client: http://localhost:8080/client/dashboard"
    echo ""
    echo "Appuyez sur Ctrl+C pour arrêter l'application"
    echo ""
    
    mvn spring-boot:run
else
    echo "❌ Erreur lors de la compilation"
    exit 1
fi
