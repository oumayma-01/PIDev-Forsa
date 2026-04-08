# 🧪 Instructions de Test - AccountController

## 📋 Prérequis

1. ✅ Projet compilé avec succès
2. ✅ Base de données MySQL fonctionnelle
3. ✅ Spring Boot démarré sur `http://localhost:8080`
4. ✅ OpenAI API Key configurée (pour tests IA)
5. ✅ Utilisateurs ADMIN et NON-ADMIN créés

---

## 🚀 Test 1: Vérifier les Endpoints dans Swagger

### Étape 1: Accéder à Swagger UI
```
URL: http://localhost:8080/swagger-ui.html
```

### Étape 2: Autorisation
1. Cliquez le bouton **"Authorize"** (en haut à droite)
2. Copiez-collez un **token JWT valide** d'un utilisateur ADMIN
3. Cliquez "Authorize" pour confirmer

### Étape 3: Vérifier les Endpoints
Recherchez dans la liste "Accounts":
- ✅ `GET /api/accounts/all` - Visible
- ✅ `GET /api/accounts/{id}` - Visible
- ✅ `DELETE /api/accounts/{id}` - Visible
- ✅ `PUT /api/accounts/{id}/status` - Visible
- ✅ `POST /api/accounts/create` - Visible
- ✅ `GET /api/accounts/owner/{ownerId}` - Visible

---

## 🧪 Test 2: Créer un Compte (Tous les Utilisateurs)

### Via Swagger
1. Recherchez `POST /api/accounts/create`
2. Cliquez "Try it out"
3. Remplissez les paramètres:
   - `ownerId`: 1
   - `type`: SAVINGS
4. Cliquez "Execute"

**Résultat Attendu:**
```json
{
  "id": 1,
  "type": "SAVINGS",
  "status": "ACTIVE",
  "wallet": {
    "id": 1,
    "ownerId": 1,
    "balance": 0.00,
    "transactions": []
  }
}
```

### Via CURL
```bash
curl -X POST "http://localhost:8080/api/accounts/create?ownerId=1&type=SAVINGS" \
  -H "Authorization: Bearer YOUR_JWT_TOKEN" \
  -H "Content-Type: application/json"
```

---

## 🧪 Test 3: Dépôt (Tous les Utilisateurs)

### Via Swagger
1. Recherchez `POST /api/accounts/{id}/deposit`
2. Cliquez "Try it out"
3. Remplissez:
   - `id`: 1
   - `amount`: 1000
4. Cliquez "Execute"

**Résultat Attendu:**
```
"Deposit successful"
```

### Via CURL
```bash
curl -X POST "http://localhost:8080/api/accounts/1/deposit?amount=1000" \
  -H "Authorization: Bearer YOUR_JWT_TOKEN"
```

---

## 🧪 Test 4: Récupérer Tous les Comptes (ADMIN ONLY)

### Avec Token ADMIN

**Via Swagger:**
1. Recherchez `GET /api/accounts/all`
2. Cliquez "Try it out"
3. Cliquez "Execute" avec token ADMIN

**Résultat Attendu:** Code 200, liste complète des comptes

### Avec Token NON-ADMIN

**Résultat Attendu:** Code 403 Forbidden
```json
{
  "error": "Access Denied",
  "message": "User does not have access to this resource"
}
```

### Via CURL
```bash
# Avec token ADMIN - Succès (200)
curl -X GET "http://localhost:8080/api/accounts/all" \
  -H "Authorization: Bearer ADMIN_TOKEN"

# Avec token NON-ADMIN - Erreur (403)
curl -X GET "http://localhost:8080/api/accounts/all" \
  -H "Authorization: Bearer USER_TOKEN"
```

---

## 🧪 Test 5: Supprimer un Compte (ADMIN ONLY)

### Avec Token ADMIN

**Via Swagger:**
1. Recherchez `DELETE /api/accounts/{id}`
2. Remplissez `id`: 1
3. Cliquez "Execute" avec token ADMIN

**Résultat Attendu:** Code 200
```
"Account deleted successfully"
```

### Avec Token NON-ADMIN

**Résultat Attendu:** Code 403 Forbidden

### Via CURL
```bash
# Avec token ADMIN - Succès
curl -X DELETE "http://localhost:8080/api/accounts/1" \
  -H "Authorization: Bearer ADMIN_TOKEN"

# Avec token NON-ADMIN - Erreur
curl -X DELETE "http://localhost:8080/api/accounts/1" \
  -H "Authorization: Bearer USER_TOKEN"
```

---

## 🧪 Test 6: Mettre à Jour le Statut (ADMIN ONLY)

### Via Swagger
1. Recherchez `PUT /api/accounts/{id}/status`
2. Remplissez:
   - `id`: 1
   - `status`: BLOCKED
3. Cliquez "Execute" avec token ADMIN

**Résultat Attendu:**
```json
{
  "id": 1,
  "status": "BLOCKED",
  ...
}
```

---

## 🧪 Test 7: Virements

### Créer deux comptes d'abord

```bash
# Compte 1 (ownerId: 1)
curl -X POST "http://localhost:8080/api/accounts/create?ownerId=1&type=SAVINGS" \
  -H "Authorization: Bearer TOKEN"

# Compte 2 (ownerId: 2)
curl -X POST "http://localhost:8080/api/accounts/create?ownerId=2&type=SAVINGS" \
  -H "Authorization: Bearer TOKEN"
```

### Dépôt sur Compte 1

```bash
curl -X POST "http://localhost:8080/api/accounts/1/deposit?amount=5000" \
  -H "Authorization: Bearer TOKEN"
```

### Effectuer le Virement

**Via Swagger:**
1. Recherchez `POST /api/accounts/transfer`
2. Remplissez:
   - `fromAccountId`: 1
   - `toAccountId`: 2
   - `amount`: 2000
3. Cliquez "Execute"

**Résultat Attendu:**
```
"Transfer successful"
```

### Vérifier les Soldes

```bash
# Compte 1 - Doit avoir 3000
curl -X GET "http://localhost:8080/api/accounts/1/statistics" \
  -H "Authorization: Bearer TOKEN"

# Compte 2 - Doit avoir 2000
curl -X GET "http://localhost:8080/api/accounts/2/statistics" \
  -H "Authorization: Bearer TOKEN"
```

---

## 🧪 Test 8: Statistiques

### Via Swagger
1. Recherchez `GET /api/accounts/{id}/statistics`
2. Remplissez `id`: 1
3. Cliquez "Execute"

**Résultat Attendu:**
```json
{
  "totalBalance": 3000.00,
  "totalDeposits": 5000.00,
  "totalWithdrawals": 0.00,
  "transactionCount": 2
}
```

---

## 🧪 Test 9: Tests IA (Optionnel - Nécessite OpenAI)

### Prévision (Forecast)

```bash
curl -X GET "http://localhost:8080/api/accounts/1/forecast?days=30" \
  -H "Authorization: Bearer TOKEN"
```

**Résultat Attendu:**
```json
{
  "currentBalance": 3000.00,
  "predictedBalance": 3100.00,
  "forecastDays": 30,
  "trend": "HAUSSE",
  "explanation": "Solde stable avec dépôts réguliers..."
}
```

### Intérêt Adaptatif

```bash
curl -X POST "http://localhost:8080/api/accounts/1/adaptive-interest" \
  -H "Authorization: Bearer ADMIN_TOKEN"
```

### Conseil de Type de Compte

```bash
curl -X GET "http://localhost:8080/api/accounts/1/account-type-advice" \
  -H "Authorization: Bearer TOKEN"
```

---

## 📊 Tableau Récapitulatif des Tests

| Test | Endpoint | Utilisateur | Résultat Attendu |
|------|----------|-----------|------------------|
| 1 | POST /create | Tous | 200 ✅ |
| 2 | POST /{id}/deposit | Tous | 200 ✅ |
| 3 | GET /all | ADMIN | 200 ✅ |
| 3bis | GET /all | NON-ADMIN | 403 ❌ |
| 4 | DELETE /{id} | ADMIN | 200 ✅ |
| 4bis | DELETE /{id} | NON-ADMIN | 403 ❌ |
| 5 | PUT /{id}/status | ADMIN | 200 ✅ |
| 5bis | PUT /{id}/status | NON-ADMIN | 403 ❌ |
| 6 | POST /transfer | Tous | 200 ✅ |
| 7 | GET /{id}/statistics | Tous | 200 ✅ |
| 8 | GET /{id}/forecast | Tous | 200 ✅ |
| 9 | POST /{id}/adaptive-interest | Tous | 200 ✅ |
| 10 | GET /{id}/account-type-advice | Tous | 200 ✅ |

---

## 🔍 Points à Vérifier

### Sécurité ✅
- [ ] NON-ADMIN ne peut pas accéder à `/all`
- [ ] NON-ADMIN ne peut pas supprimer (`DELETE`)
- [ ] NON-ADMIN ne peut pas modifier le statut
- [ ] ADMIN peut faire toutes les opérations

### Fonctionnalités ✅
- [ ] Création de compte fonctionne
- [ ] Dépôts/Retraits fonctionnent
- [ ] Virements fonctionnent
- [ ] Statistiques correctes
- [ ] Historique d'activité complet

### Validations ✅
- [ ] Montant négatif = erreur
- [ ] Retrait plus que solde = erreur
- [ ] Compte bloqué = erreur sur opérations
- [ ] Virement vers même compte = erreur

### Documentation ✅
- [ ] Tous les endpoints visibles dans Swagger
- [ ] Authentification requise (Bearer token)
- [ ] Restrictions de rôle affichées

---

## 🐛 Débogage

### Voir les Logs
```bash
tail -f application.log | grep "Account"
```

### Vérifier la Base de Données
```sql
SELECT * FROM account;
SELECT * FROM wallet;
SELECT * FROM transaction;
SELECT * FROM activity;
```

### Tester sans Swagger (PostMan)

1. Importez les URLs
2. Ajoutez le header: `Authorization: Bearer TOKEN`
3. Testez chaque endpoint

---

## ✨ Conclusion

**Tous ces tests passés = ✅ Système prêt en production!**

