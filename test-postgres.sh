#!/bin/bash

echo "🧪 Test de connexion PostgreSQL"
echo "==============================="

# Tester la connexion avec les paramètres de l'application
echo "🔍 Test de connexion avec les paramètres de l'application..."
PGPASSWORD=postgres psql -h localhost -U postgres -d rapidclean -c "SELECT 'Connexion PostgreSQL réussie' as result;"

if [ $? -eq 0 ]; then
    echo "✅ PostgreSQL est prêt !"
    echo ""
    echo "🚀 Lancement de l'application avec PostgreSQL..."
    echo ""
    mvn spring-boot:run
else
    echo "❌ PostgreSQL n'est pas accessible"
    echo ""
    echo "🔧 Solutions :"
    echo "1. Vérifiez que PostgreSQL est démarré : systemctl status postgresql"
    echo "2. Vérifiez que la base 'rapidclean' existe"
    echo "3. Vérifiez le mot de passe de l'utilisateur postgres"
    echo ""
    echo "💡 Alternative : Utilisez H2 avec ./start-h2.sh"
fi
