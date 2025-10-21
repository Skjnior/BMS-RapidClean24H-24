#!/bin/bash

echo "🧹 BMS Rapid Clean - Service de Nettoyage 24h/24"
echo "================================================"
echo "🐘 Utilisation de PostgreSQL"
echo ""

# Vérifier Java
if command -v java &> /dev/null; then
    JAVA_VERSION=$(java -version 2>&1 | head -n 1 | cut -d'"' -f2 | cut -d'.' -f1)
    echo "✅ Java version $JAVA_VERSION détectée"
else
    echo "❌ Java n'est pas installé"
    exit 1
fi

# Vérifier Maven
if command -v mvn &> /dev/null; then
    echo "✅ Maven détecté"
else
    echo "❌ Maven n'est pas installé"
    exit 1
fi

# Vérifier PostgreSQL
echo "🔍 Vérification de PostgreSQL..."
if systemctl is-active --quiet postgresql; then
    echo "✅ PostgreSQL est en cours d'exécution"
else
    echo "❌ PostgreSQL n'est pas démarré"
    echo "🚀 Démarrage de PostgreSQL..."
    sudo systemctl start postgresql
    if systemctl is-active --quiet postgresql; then
        echo "✅ PostgreSQL démarré avec succès"
    else
        echo "❌ Impossible de démarrer PostgreSQL"
        exit 1
    fi
fi

# Tester la connexion PostgreSQL
echo "🧪 Test de connexion PostgreSQL..."
if PGPASSWORD=toor psql -h localhost -U kaba -d rapidclean -c "SELECT 1;" &> /dev/null; then
    echo "✅ Connexion PostgreSQL réussie"
else
    echo "❌ Connexion PostgreSQL échouée"
    echo "💡 Vérifiez que la base 'rapidclean' et l'utilisateur 'kaba' existent"
    exit 1
fi

# Compiler l'application
echo "🔨 Compilation de l'application..."
if mvn clean compile -q; then
    echo "✅ Compilation réussie"
else
    echo "❌ Erreur lors de la compilation"
    exit 1
fi

echo ""
echo "🚀 Lancement de l'application avec PostgreSQL..."
echo "🌐 L'application sera accessible sur: http://localhost:8999"
echo "📧 Interface Admin: http://localhost:8999/admin/dashboard"
echo "👤 Interface Client: http://localhost:8999/client/dashboard"
echo ""
echo "🔑 Comptes par défaut :"
echo "   • Admin: admin@bmsrapidclean.com / admin123"
echo "   • Client: client@example.com / client123"
echo ""
echo "Appuyez sur Ctrl+C pour arrêter l'application"
echo ""

# Lancer avec PostgreSQL
mvn spring-boot:run
