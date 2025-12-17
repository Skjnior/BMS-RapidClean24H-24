#!/bin/bash
# Script de test pour vérifier le backup PostgreSQL

echo "=== Test de Backup PostgreSQL ==="
echo ""

# Configuration (à adapter selon votre configuration)
DB_HOST="localhost"
DB_PORT="5432"
DB_NAME="rapidClean"
DB_USER="kaba"
DB_PASSWORD="toor"
BACKUP_DIR="./backups"
TIMESTAMP=$(date +"%Y-%m-%d_%H-%M-%S")
BACKUP_FILE="${BACKUP_DIR}/test_backup_${TIMESTAMP}.sql"

# Créer le répertoire de backup s'il n'existe pas
mkdir -p "${BACKUP_DIR}"

echo "1. Vérification de la connexion à la base de données..."
export PGPASSWORD="${DB_PASSWORD}"
psql -h "${DB_HOST}" -p "${DB_PORT}" -U "${DB_USER}" -d "${DB_NAME}" -c "SELECT COUNT(*) as total_tables FROM information_schema.tables WHERE table_schema = 'public';" 2>&1

if [ $? -ne 0 ]; then
    echo "❌ Erreur: Impossible de se connecter à la base de données"
    exit 1
fi

echo ""
echo "2. Vérification du contenu de la base de données..."
psql -h "${DB_HOST}" -p "${DB_PORT}" -U "${DB_USER}" -d "${DB_NAME}" -c "
SELECT 
    'users' as table_name, COUNT(*) as row_count FROM users
UNION ALL
SELECT 'services', COUNT(*) FROM services
UNION ALL
SELECT 'service_requests', COUNT(*) FROM service_requests
UNION ALL
SELECT 'contact_messages', COUNT(*) FROM contact_messages
UNION ALL
SELECT 'reviews', COUNT(*) FROM reviews
UNION ALL
SELECT 'notifications', COUNT(*) FROM notifications
UNION ALL
SELECT 'time_tracking', COUNT(*) FROM time_tracking
UNION ALL
SELECT 'absences', COUNT(*) FROM absences
UNION ALL
SELECT 'workplace_observations', COUNT(*) FROM workplace_observations
UNION ALL
SELECT 'audit_logs', COUNT(*) FROM audit_logs;
" 2>&1

echo ""
echo "3. Création du backup..."
pg_dump -h "${DB_HOST}" -p "${DB_PORT}" -U "${DB_USER}" -d "${DB_NAME}" \
    -F p \
    --no-owner \
    --no-acl \
    --clean \
    --if-exists \
    --verbose \
    -f "${BACKUP_FILE}" 2>&1

EXIT_CODE=$?

if [ $EXIT_CODE -eq 0 ]; then
    if [ -f "${BACKUP_FILE}" ]; then
        FILE_SIZE=$(stat -f%z "${BACKUP_FILE}" 2>/dev/null || stat -c%s "${BACKUP_FILE}" 2>/dev/null || echo "0")
        echo ""
        echo "   Fichier: ${BACKUP_FILE}"
        echo "   Taille: ${FILE_SIZE} octets"
        
        if [ "$FILE_SIZE" -eq 0 ]; then
            echo "⚠️  ATTENTION: Le fichier de backup est vide (0 octets)!"
            echo "   Cela peut signifier que la base de données est vide ou qu'il y a eu une erreur."
        else
            echo ""
            echo "4. Aperçu du contenu du backup (premières 20 lignes):"
            head -20 "${BACKUP_FILE}"
        fi
    else
        echo "❌ Erreur: Le fichier de backup n'a pas été créé"
        exit 1
    fi
else
    echo "❌ Erreur lors de la création du backup (code de sortie: ${EXIT_CODE})"
    exit 1
fi

echo ""
echo "=== Test terminé ==="



