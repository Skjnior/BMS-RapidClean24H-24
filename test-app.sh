#!/bin/bash

echo "🧪 Test de l'application BMS Rapid Clean"
echo "========================================"

# Vérifier si l'application est en cours d'exécution
if curl -s http://localhost:8999 > /dev/null; then
    echo "✅ Application accessible sur http://localhost:8999"
    echo ""
    echo "🌐 Pages disponibles :"
    echo "   • Landing Page: http://localhost:8999"
    echo "   • Admin Dashboard: http://localhost:8999/admin/dashboard"
    echo "   • Client Dashboard: http://localhost:8999/client/dashboard"
    echo "   • Console H2: http://localhost:8999/h2-console (si H2)"
    echo ""
    echo "🔑 Comptes par défaut :"
    echo "   • Admin: admin / admin"
    echo "   • Client: client / client"
    echo ""
    echo "📊 Base de données :"
    if curl -s http://localhost:8999/h2-console > /dev/null; then
        echo "   • H2 Console: http://localhost:8999/h2-console"
        echo "   • URL: jdbc:h2:mem:rapidclean"
        echo "   • Username: sa"
        echo "   • Password: (vide)"
    else
        echo "   • PostgreSQL configuré"
    fi
else
    echo "❌ Application non accessible"
    echo ""
    echo "🚀 Pour démarrer l'application :"
    echo "   ./start-h2.sh      # Avec H2 (recommandé)"
    echo "   ./start-final.sh   # Script universel"
    echo "   ./start-postgres.sh # Avec PostgreSQL"
fi
