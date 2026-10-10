#!/usr/bin/env bash
# =============================================================================
#  Test end-to-end du backend Hrayfi.
#  Enchaîne : inscription client + artisan → login → création demande →
#             accept → terminer, plus 2 cas d'erreur attendus (unauthorized,
#             transition invalide).
#  Pré-requis : app démarrée sur ${BASE_URL}, outils `curl` et `jq`.
#  Usage      : ./scripts/smoke-test.sh [BASE_URL]
#               BASE_URL par défaut : http://localhost:8080
# =============================================================================

set -euo pipefail

BASE_URL="${1:-http://localhost:8080}"

# ---------- couleurs ----------
if [[ -t 1 ]]; then
    GREEN='\033[0;32m'; RED='\033[0;31m'; BLUE='\033[0;34m'
    YELLOW='\033[0;33m'; BOLD='\033[1m'; NC='\033[0m'
else
    GREEN=''; RED=''; BLUE=''; YELLOW=''; BOLD=''; NC=''
fi

STEP=0
ok()     { echo -e "${GREEN}✓${NC} $*"; }
fail()   { echo -e "${RED}✗ $*${NC}"; exit 1; }
info()   { echo -e "${BLUE}ℹ${NC} $*"; }
step()   { STEP=$((STEP+1)); echo -e "\n${BOLD}${YELLOW}── Étape ${STEP} : $* ──${NC}"; }

# ---------- pré-vol ----------
command -v curl >/dev/null || fail "curl n'est pas installé"
command -v jq   >/dev/null || fail "jq n'est pas installé  (sudo apt install jq)"

info "Base URL : ${BASE_URL}"
info "Ping /v3/api-docs ..."
if ! curl -sf "${BASE_URL}/v3/api-docs" > /dev/null; then
    fail "L'API ne répond pas. Démarre d'abord : ./mvnw spring-boot:run"
fi
ok "API joignable"

# ---------- helpers ----------
# wrap curl : vérifie le code HTTP, imprime la réponse en JSON indenté
# Usage : http_call METHOD URL [--token TOKEN] [--data JSON] [--expect STATUS]
http_call() {
    local method=$1; shift
    local url=$1; shift
    local token="" data="" expect="200"
    while [[ $# -gt 0 ]]; do
        case "$1" in
            --token)  token=$2; shift 2;;
            --data)   data=$2;  shift 2;;
            --expect) expect=$2; shift 2;;
            *) fail "arg inconnu http_call: $1";;
        esac
    done

    local args=(-sS -o /tmp/resp.json -w "%{http_code}" -X "$method"
                -H "Content-Type: application/json"
                -H "Accept: application/json")
    [[ -n "$token" ]] && args+=(-H "Authorization: Bearer ${token}")
    [[ -n "$data"  ]] && args+=(--data "$data")

    local code
    code=$(curl "${args[@]}" "${BASE_URL}${url}")
    if [[ "$code" != "$expect" ]]; then
        echo -e "${RED}Réponse HTTP ${code} (attendu ${expect})${NC}"
        cat /tmp/resp.json 2>/dev/null | jq . 2>/dev/null || cat /tmp/resp.json
        fail "Appel ${method} ${url} a échoué"
    fi
    cat /tmp/resp.json
}

# ---------- data ----------
# Suffixe unique pour pouvoir ré-exécuter le script sans conflit d'email
TS=$(date +%s)
CLIENT_EMAIL="client-${TS}@test.io"
ARTISAN_EMAIL="artisan-${TS}@test.io"
INTERVENTION_DATE=$(date -u -d '+3 days' +%Y-%m-%dT%H:%M:%SZ 2>/dev/null \
                 || date -u -v+3d +%Y-%m-%dT%H:%M:%SZ)   # macOS fallback

# =============================================================================
step "Inscription du CLIENT"
# =============================================================================
CLIENT_PAYLOAD=$(jq -nc --arg e "$CLIENT_EMAIL" '{
    email: $e,
    password: "password123",
    nom: "Dupont",
    prenom: "Jean",
    adresse: "1 rue de la paix, Casablanca",
    preferences: "Rapide et ponctuel"
}')
CLIENT_RESP=$(http_call POST /api/auth/register/client --data "$CLIENT_PAYLOAD" --expect 201)
CLIENT_TOKEN=$(echo "$CLIENT_RESP" | jq -r '.accessToken')
CLIENT_ID=$(echo    "$CLIENT_RESP" | jq -r '.userId')
ok "Client créé  id=${CLIENT_ID}  email=${CLIENT_EMAIL}"

# =============================================================================
step "Inscription de l'ARTISAN"
# =============================================================================
ARTISAN_PAYLOAD=$(jq -nc --arg e "$ARTISAN_EMAIL" '{
    email: $e,
    password: "password123",
    nom: "Benali",
    prenom: "Karim",
    metier: "Plombier",
    tarifHoraire: 150.00,
    description: "Plombier agréé, intervention rapide",
    ville: "Casablanca"
}')
ARTISAN_RESP=$(http_call POST /api/auth/register/artisan --data "$ARTISAN_PAYLOAD" --expect 201)
ARTISAN_TOKEN=$(echo "$ARTISAN_RESP" | jq -r '.accessToken')
ARTISAN_ID=$(echo    "$ARTISAN_RESP" | jq -r '.userId')
ok "Artisan créé  id=${ARTISAN_ID}  email=${ARTISAN_EMAIL}"

# =============================================================================
step "Login du client (vérifie que login + bcrypt fonctionnent)"
# =============================================================================
LOGIN_PAYLOAD=$(jq -nc --arg e "$CLIENT_EMAIL" '{email: $e, password: "password123"}')
http_call POST /api/auth/login --data "$LOGIN_PAYLOAD" > /dev/null
ok "Login OK"

# =============================================================================
step "Login avec mauvais mot de passe → doit renvoyer 401"
# =============================================================================
BAD_LOGIN=$(jq -nc --arg e "$CLIENT_EMAIL" '{email: $e, password: "wrong-password"}')
http_call POST /api/auth/login --data "$BAD_LOGIN" --expect 401 > /dev/null
ok "401 renvoyé correctement sur mauvais mot de passe"

# =============================================================================
step "CLIENT crée une demande pour l'artisan"
# =============================================================================
DEMANDE_PAYLOAD=$(jq -nc \
    --argjson aid "$ARTISAN_ID" \
    --arg date "$INTERVENTION_DATE" \
    '{
       artisanId: $aid,
       description: "Fuite sous l'\''évier de la cuisine, besoin d'\''un plombier urgent.",
       dateIntervention: $date
     }')
DEMANDE_RESP=$(http_call POST /api/demandes --token "$CLIENT_TOKEN" --data "$DEMANDE_PAYLOAD" --expect 201)
DEMANDE_ID=$(echo "$DEMANDE_RESP" | jq -r '.id')
STATUT=$(echo    "$DEMANDE_RESP" | jq -r '.statut')
[[ "$STATUT" == "EN_ATTENTE" ]] || fail "Statut initial attendu EN_ATTENTE, reçu : $STATUT"
ok "Demande créée  id=${DEMANDE_ID}  statut=${STATUT}"

# =============================================================================
step "ARTISAN tente d'accepter la demande d'un AUTRE artisan → 403"
# =============================================================================
# On s'inscrit comme un second artisan et on vérifie qu'il n'a pas le droit
OTHER_PAYLOAD=$(jq -nc --arg e "autre-artisan-${TS}@test.io" '{
    email: $e, password: "password123", nom: "X", prenom: "Y",
    metier: "Peintre", tarifHoraire: 100.0, description: "", ville: "Rabat"
}')
OTHER_TOKEN=$(http_call POST /api/auth/register/artisan --data "$OTHER_PAYLOAD" --expect 201 | jq -r '.accessToken')
http_call PATCH "/api/demandes/${DEMANDE_ID}/accepter" --token "$OTHER_TOKEN" --expect 403 > /dev/null
ok "403 renvoyé : l'artisan non ciblé ne peut pas accepter"

# =============================================================================
step "CLIENT tente d'accepter sa propre demande → 403 (role insuffisant)"
# =============================================================================
http_call PATCH "/api/demandes/${DEMANDE_ID}/accepter" --token "$CLIENT_TOKEN" --expect 403 > /dev/null
ok "403 renvoyé : un CLIENT ne peut pas accepter"

# =============================================================================
step "ARTISAN cible accepte la demande"
# =============================================================================
RESP=$(http_call PATCH "/api/demandes/${DEMANDE_ID}/accepter" --token "$ARTISAN_TOKEN")
STATUT=$(echo "$RESP" | jq -r '.statut')
[[ "$STATUT" == "ACCEPTEE" ]] || fail "Statut attendu ACCEPTEE, reçu : $STATUT"
ok "Statut = ACCEPTEE"

# =============================================================================
step "ARTISAN tente de ré-accepter → 409 (transition invalide)"
# =============================================================================
http_call PATCH "/api/demandes/${DEMANDE_ID}/accepter" --token "$ARTISAN_TOKEN" --expect 409 > /dev/null
ok "409 Conflict renvoyé : on ne peut pas accepter deux fois"

# =============================================================================
step "ARTISAN termine la demande avec un prix final"
# =============================================================================
TERMINER_PAYLOAD='{"prixFinal": 450.50}'
RESP=$(http_call PATCH "/api/demandes/${DEMANDE_ID}/terminer" \
       --token "$ARTISAN_TOKEN" --data "$TERMINER_PAYLOAD")
STATUT=$(echo "$RESP" | jq -r '.statut')
PRIX=$(echo   "$RESP" | jq -r '.prixFinal')
[[ "$STATUT" == "TERMINEE" ]] || fail "Statut attendu TERMINEE, reçu : $STATUT"
ok "Statut = TERMINEE  prixFinal=${PRIX}"

# =============================================================================
step "CLIENT liste ses demandes"
# =============================================================================
MES_DEMANDES=$(http_call GET /api/demandes/mes-demandes --token "$CLIENT_TOKEN")
COUNT=$(echo "$MES_DEMANDES" | jq -r '.content | length')
[[ "$COUNT" -ge 1 ]] || fail "Le client devrait voir au moins 1 demande"
ok "Le client voit ${COUNT} demande(s)"

# =============================================================================
step "ARTISAN liste les demandes reçues"
# =============================================================================
RECUES=$(http_call GET /api/demandes/recues --token "$ARTISAN_TOKEN")
COUNT=$(echo "$RECUES" | jq -r '.content | length')
[[ "$COUNT" -ge 1 ]] || fail "L'artisan devrait voir au moins 1 demande"
ok "L'artisan voit ${COUNT} demande(s)"

# =============================================================================
step "Accès sans token → 403"
# =============================================================================
http_call GET /api/demandes/mes-demandes --expect 403 > /dev/null
ok "Endpoint protégé bien verrouillé"

# =============================================================================
step "Validation : création d'une demande sans description → 400"
# =============================================================================
BAD=$(jq -nc --argjson aid "$ARTISAN_ID" --arg d "$INTERVENTION_DATE" \
      '{artisanId: $aid, description: "", dateIntervention: $d}')
http_call POST /api/demandes --token "$CLIENT_TOKEN" --data "$BAD" --expect 400 > /dev/null
ok "400 renvoyé sur payload invalide (Bean Validation OK)"

# =============================================================================
echo -e "\n${BOLD}${GREEN}✅ Tous les tests passent (${STEP} étapes).${NC}"
echo    "   Client   : id=${CLIENT_ID}  email=${CLIENT_EMAIL}"
echo    "   Artisan  : id=${ARTISAN_ID} email=${ARTISAN_EMAIL}"
echo    "   Demande  : id=${DEMANDE_ID} statut=TERMINEE prixFinal=${PRIX}"