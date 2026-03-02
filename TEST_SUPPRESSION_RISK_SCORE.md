# Tests de Validation - Suppression du champ riskScore

## Objectif
Vérifier que toutes les fonctionnalités liées au scoring fonctionnent correctement après la suppression du champ `riskScore`.

## Prérequis

1. ✅ Base de données MySQL démarrée
2. ✅ Migration SQL exécutée (suppression de la colonne `risk_score`)
3. ✅ API IA de scoring opérationnelle sur `http://localhost:8000`
4. ✅ Application Spring Boot démarrée sur `http://localhost:8089`

## Tests à effectuer

### 1. Test de compilation
```bash
cd D:\PIDev-Forsa
.\mvnw.cmd clean compile -DskipTests
```

**Résultat attendu** :
```
[INFO] BUILD SUCCESS
```

✅ **Status** : Validé

---

### 2. Test de démarrage de l'application

```bash
.\mvnw.cmd spring-boot:run
```

**Vérifications** :
- ✅ L'application démarre sans erreur
- ✅ Aucune erreur Hibernate liée à `risk_score`
- ✅ Les tables sont créées/mises à jour correctement

**Logs attendus** :
```
Started ForsaPidevApplication in X.XXX seconds
```

---

### 3. Test d'authentification JWT

**Endpoint** : `POST /forsaPidev/api/auth/signin`

**Request** :
```json
{
  "username": "sarra",
  "password": "sarra123"
}
```

**Curl** :
```bash
curl -X POST http://localhost:8089/forsaPidev/api/auth/signin \
  -H "Content-Type: application/json" \
  -d '{"username":"sarra","password":"sarra123"}'
```

**Résultat attendu** :
```json
{
  "token": "eyJhbGci...",
  "type": "Bearer",
  "id": 44,
  "username": "sarra",
  "email": "...",
  "role": "CLIENT"
}
```

**Sauvegarder le token** : `TOKEN=<token reçu>`

---

### 4. Test de création d'une demande de crédit

**Endpoint** : `POST /forsaPidev/api/credits`

**Request** :
```json
{
  "amountRequested": 50000,
  "durationMonths": 24,
  "typeCalcul": "ANNUITE_CONSTANTE"
}
```

**Curl** :
```bash
curl -X POST http://localhost:8089/forsaPidev/api/credits \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "amountRequested": 50000,
    "durationMonths": 24,
    "typeCalcul": "ANNUITE_CONSTANTE"
  }'
```

**Vérifications** :
- ✅ Le crédit est créé avec `status = "SUBMITTED"` ou `"UNDER_REVIEW"`
- ✅ Les champs suivants sont présents dans la réponse :
  - `isRisky` : `true` ou `false`
  - `riskLevel` : `"LOW"`, `"MEDIUM"` ou `"HIGH"`
  - `scoredAt` : `"2026-03-01T..."` (date actuelle)
- ❌ Le champ `riskScore` **NE DOIT PAS** être présent dans la réponse

**Réponse attendue** :
```json
{
  "id": 85,
  "amountRequested": 50000,
  "interestRate": 5.5,
  "durationMonths": 24,
  "status": "UNDER_REVIEW",
  "requestDate": "2026-03-01T...",
  "isRisky": false,
  "riskLevel": "LOW",
  "scoredAt": "2026-03-01T...",
  "typeCalcul": "ANNUITE_CONSTANTE"
}
```

**Logs attendus** :
```
INFO : Création d'une nouvelle demande de crédit pour l'utilisateur sarra avec montant 50000
INFO : Lancement du scoring IA pour la demande de crédit ID=85
INFO : Features calculées : avgDelay=0.0, instability=0.0
INFO : Appel du service IA de scoring sur http://localhost:8000/predict-with-report
INFO : Scoring terminé pour crédit ID=85 : risky=false, level=LOW
INFO : Demande de crédit créée avec succès - ID=85, Risque=LOW
```

---

### 5. Test de validation d'une demande de crédit

**Endpoint** : `POST /forsaPidev/api/credits/{id}/validate`

**Curl** :
```bash
curl -X POST http://localhost:8089/forsaPidev/api/credits/85/validate \
  -H "Authorization: Bearer $TOKEN"
```

**Vérifications** :
- ✅ Le crédit passe en statut `"APPROVED"`
- ✅ Le tableau d'amortissement est généré
- ✅ Les champs `isRisky`, `riskLevel`, `scoredAt` sont toujours présents
- ❌ Aucune référence à `riskScore` dans les logs

**Logs attendus** :
```
INFO : Validation de la demande de crédit ID=85
INFO : Crédit ID=85 validé avec succès
```

---

### 6. Test de récupération d'un crédit

**Endpoint** : `GET /forsaPidev/api/credits/{id}`

**Curl** :
```bash
curl -X GET http://localhost:8089/forsaPidev/api/credits/85 \
  -H "Authorization: Bearer $TOKEN"
```

**Vérifications** :
- ✅ Le crédit est retourné avec tous les champs
- ✅ `isRisky`, `riskLevel`, `scoredAt` sont présents
- ❌ `riskScore` n'est **PAS** présent dans la réponse JSON

---

### 7. Test de vérification de la base de données

**SQL** :
```sql
-- Vérifier la structure de la table
DESCRIBE credit_request;

-- Vérifier qu'un crédit a bien les champs de scoring
SELECT id, amount_requested, status, is_risky, risk_level, scored_at
FROM credit_request
WHERE id = 85;
```

**Résultats attendus** :
- ✅ La colonne `risk_score` n'existe **PAS** dans la table
- ✅ Les colonnes `is_risky`, `risk_level`, `scored_at` existent
- ✅ Les valeurs sont correctement renseignées

---

### 8. Test du système de re-rating (ScoreResult)

**Note** : Ce système est indépendant et ne devrait **PAS** être affecté.

**Vérification** :
- ✅ La table `score_result` existe toujours
- ✅ Elle contient toujours `final_score`
- ✅ Le service `ReRatingService` fonctionne normalement

---

## Checklist de validation finale

| Test | Status | Remarques |
|------|--------|-----------|
| Compilation sans erreur | ✅ | BUILD SUCCESS |
| Démarrage application | ⏳ | À tester |
| Authentification JWT | ⏳ | À tester |
| Création crédit + scoring | ⏳ | À tester |
| Champs présents : isRisky, riskLevel, scoredAt | ⏳ | À tester |
| Champ absent : riskScore | ⏳ | À tester |
| Validation crédit | ⏳ | À tester |
| Logs corrects (sans référence au score) | ⏳ | À tester |
| DB : colonne risk_score supprimée | ⏳ | À tester |
| Système ScoreResult non affecté | ⏳ | À tester |

---

## Erreurs potentielles et solutions

### Erreur 1 : Colonne `risk_score` existe toujours en DB
**Message** : `Column 'risk_score' cannot be null`

**Solution** :
```sql
ALTER TABLE credit_request DROP COLUMN IF EXISTS risk_score;
```

### Erreur 2 : Référence à getRiskScore() dans un contrôleur
**Message** : `Cannot resolve method 'getRiskScore'`

**Solution** : Rechercher et supprimer toutes les références
```bash
grep -r "getRiskScore" src/
grep -r "setRiskScore" src/
```

### Erreur 3 : Service IA ne retourne pas risk_level
**Message** : Log indiquant `riskLevel=null`

**Solution** : Vérifier que l'API IA retourne bien `risk_level` dans la réponse

---

## Résultat final attendu

✅ **Toutes les fonctionnalités de scoring fonctionnent**
✅ **Le champ riskScore a été complètement supprimé**
✅ **Les champs isRisky, riskLevel, scoredAt suffisent pour la gestion métier**
✅ **Aucune régression sur les autres fonctionnalités**

