#!/bin/bash

echo "🔧 Configuration PostgreSQL pour BMS Rapid Clean"
echo "================================================"

# Vérifier si PostgreSQL est en cours d'exécution
if ! systemctl is-active --quiet postgresql; then
    echo "🚀 Démarrage de PostgreSQL..."
    sudo systemctl start postgresql
fi

echo "✅ PostgreSQL est en cours d'exécution"

# Méthode 1: Configuration via psql
echo "🔐 Méthode 1: Configuration via psql"
echo "Exécutez les commandes suivantes :"
echo ""
echo "sudo -u postgres psql"
echo "ALTER USER postgres PASSWORD 'postgres';"
echo "\\q"
echo ""

# Méthode 2: Configuration via pg_hba.conf
echo "🔐 Méthode 2: Configuration via pg_hba.conf"
echo "1. Éditez le fichier de configuration :"
echo "   sudo nano /etc/postgresql/*/main/pg_hba.conf"
echo ""
echo "2. Changez la ligne :"
echo "   local   all             all                                     peer"
echo "   en :"
echo "   local   all             all                                     md5"
echo ""
echo "3. Redémarrez PostgreSQL :"
echo "   sudo systemctl restart postgresql"
echo ""

# Méthode 3: Créer un utilisateur spécifique
echo "🔐 Méthode 3: Créer un utilisateur spécifique"
echo "sudo -u postgres psql"
echo "CREATE USER rapidclean_user WITH PASSWORD 'rapidclean_pass';"
echo "CREATE DATABASE rapidclean OWNER rapidclean_user;"
echo "GRANT ALL PRIVILEGES ON DATABASE rapidclean TO rapidclean_user;"
echo "\\q"
echo ""

# Test de connexion
echo "🧪 Test de connexion..."
if PGPASSWORD=postgres psql -h localhost -U postgres -d rapidclean -c "SELECT 1;" &> /dev/null; then
    echo "✅ PostgreSQL est configuré correctement !"
    echo "🚀 Vous pouvez maintenant utiliser :"
    echo "   ./start-postgres.sh"
    echo "   ou"
    echo "   ./start-final.sh"
else
    echo "❌ PostgreSQL n'est pas encore configuré"
    echo "💡 Suivez une des méthodes ci-dessus"
    echo ""
    echo "🔄 En attendant, utilisez H2 :"
    echo "   ./start-h2.sh"
fi
