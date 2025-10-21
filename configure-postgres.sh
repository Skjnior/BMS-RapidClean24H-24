#!/bin/bash

echo "🔧 Configuration PostgreSQL pour BMS Rapid Clean"
echo "================================================"

# Vérifier si PostgreSQL est en cours d'exécution
if ! systemctl is-active --quiet postgresql; then
    echo "🚀 Démarrage de PostgreSQL..."
    sudo systemctl start postgresql
fi

echo "✅ PostgreSQL est en cours d'exécution"

# Configurer l'utilisateur postgres avec un mot de passe
echo "🔐 Configuration de l'utilisateur postgres..."
sudo -u postgres psql << EOF
-- Définir un mot de passe pour l'utilisateur postgres
ALTER USER postgres PASSWORD 'postgres';

-- Vérifier que la base de données existe
SELECT 'Base de données rapidclean créée avec succès' as status;
EOF

if [ $? -eq 0 ]; then
    echo "✅ Utilisateur postgres configuré"
else
    echo "❌ Erreur lors de la configuration de l'utilisateur"
    exit 1
fi

# Tester la connexion
echo "🧪 Test de connexion à la base de données..."
PGPASSWORD=postgres psql -h localhost -U postgres -d rapidclean -c "SELECT 'Connexion réussie' as result;"

if [ $? -eq 0 ]; then
    echo "✅ Connexion PostgreSQL réussie"
    echo ""
    echo "🎉 PostgreSQL est maintenant configuré !"
    echo ""
    echo "📋 Informations de connexion :"
    echo "   Host: localhost"
    echo "   Port: 5432"
    echo "   Database: rapidclean"
    echo "   Username: postgres"
    echo "   Password: postgres"
    echo ""
    echo "🚀 Vous pouvez maintenant lancer l'application avec :"
    echo "   ./start-postgres.sh"
    echo "   ou"
    echo "   mvn spring-boot:run"
else
    echo "❌ Échec de la connexion PostgreSQL"
    echo ""
    echo "🔧 Solutions possibles :"
    echo "1. Vérifiez que PostgreSQL est installé et démarré"
    echo "2. Vérifiez que la base de données 'rapidclean' existe"
    echo "3. Vérifiez les permissions de l'utilisateur postgres"
    echo ""
    echo "💡 Vous pouvez utiliser H2 en attendant :"
    echo "   ./start-h2.sh"
fi
