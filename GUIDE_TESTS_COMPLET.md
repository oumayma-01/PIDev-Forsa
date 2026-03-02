# 🧪 GUIDE DE TEST ET VÉRIFICATION - SYSTÈME COMPLET

## 📋 Checklist de vérification avant tests

### 1. Vérifier que le projet compile
```powershell
cd d:\PIDev-Forsa
./mvnw.cmd clean compile
```

**Résultat attendu:** `BUILD SUCCESS`

---

### 2. Créer les tables en base de données

```sql
-- Table Agent
CREATE TABLE agent (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL UNIQUE,
    full_name VARCHAR(255) NOT NULL,
    is_busy BOOLEAN NOT NULL DEFAULT FALSE,
    current_assigned_request_id BIGINT,
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at DATETIME NOT NULL,
    updated_at DATETIME,
    FOREIGN KEY (user_id) REFERENCES user(id) ON DELETE CASCADE
);

CREATE INDEX idx_agent_available ON agent(is_active, is_busy);

-- Table Gift
CREATE TABLE gift (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    client_id BIGINT NOT NULL,
    accumulated_amount DECIMAL(18,2) NOT NULL DEFAULT 0.00,
    threshold DECIMAL(18,2) NOT NULL DEFAULT 500.00,
    awarded BOOLEAN NOT NULL DEFAULT FALSE,
    awarded_at DATETIME,
    awarded_amount DECIMAL(18,2),
    created_at DATETIME NOT NULL,
    updated_at DATETIME
);

CREATE INDEX idx_gift_client ON gift(client_id);
CREATE INDEX idx_gift_pending ON gift(awarded, accumulated_amount);

-- Ajouter colonne line_type à repayment_schedule
ALTER TABLE repayment_schedule 
ADD COLUMN line_type VARCHAR(20) NOT NULL DEFAULT 'NORMAL';

CREATE INDEX idx_schedule_line_type ON repayment_schedule(line_type);
```

---

### 3. Insérer des agents de test

```sql
-- Supposons que vous avez des users avec role AGENT (id 10, 11, 12)
INSERT INTO agent (user_id, full_name, is_busy, is_active, created_at) VALUES
(10, 'Agent Smith', FALSE, TRUE, NOW()),
(11, 'Agent Johnson', FALSE, TRUE, NOW()),
(12, 'Agent Williams', FALSE, TRUE, NOW());
```

---

### 4. Démarrer le service IA Python

```powershell
# Dans le dossier du projet IA
cd /chemin/vers/ia-project
python run.py
```

**Vérifier que les services sont up:**
- http://localhost:8000/health
- http://localhost:8001/health (assurance)

---

### 5. Démarrer l'application Spring Boot

```powershell
cd d:\PIDev-Forsa
./mvnw.cmd spring-boot:run
```

**Vérifier dans les logs:**
- Port 8089 actif
- `@EnableScheduling` activé
- Connexion DB OK

---

## 🧪 TESTS FONCTIONNELS

### Test 1 : Authentification et obtention du token

**1.1 Créer un utilisateur client (si nécessaire)**

```bash
curl -X POST "http://localhost:8089/forsaPidev/api/auth/signup" \
  -H "Content-Type: application/json" \
  -d '{
    "username": "testclient",
    "email": "client@test.com",
    "password": "Test@1234",
    "role": "CLIENT"
  }'
```

**1.2 Se connecter et récupérer le token**

```bash
curl -X POST "http://localhost:8089/forsaPidev/api/auth/signin" \
  -H "Content-Type: application/json" \
  -d '{
    "username": "testclient",
    "password": "Test@1234"
  }'
```

**Résultat attendu:**
```json
{
  "token": "eyJhbGciOiJIUzUxMiJ9...",
  "type": "Bearer",
  "id": 44,
  "username": "testclient",
  "email": "client@test.com",
  "roles": ["ROLE_CLIENT"]
}
```

**💾 Sauvegarder le token** pour les prochaines requêtes.

---

### Test 2 : Créer une demande de crédit avec rapport médical

**Préparer un fichier PDF de test:**
- Créer un fichier `health_report_test.pdf` (peut être vide pour le test)

**Requête multipart:**

```bash
curl -X POST "http://localhost:8089/forsaPidev/api/credits/with-health-report" \
  -H "Authorization: Bearer <VOTRE_TOKEN>" \
  -F "amountRequested=100000" \
  -F "durationMonths=12" \
  -F "typeCalcul=AMORTISSEMENT_CONSTANT" \
  -F "healthReport=@health_report_test.pdf"
```

**Résultat attendu:**
```json
{
  "id": 85,
  "amountRequested": 100000,
  "interestRate": 7.5,
  "durationMonths": 12,
  "status": "UNDER_REVIEW",
  "agentId": 10,
  "user": { "id": 44, "username": "testclient" },
  "typeCalcul": "AMORTISSEMENT_CONSTANT",
  "isRisky": false,
  "riskLevel": "LOW",
  "insuranceRate": 1.5,
  "insuranceIsReject": false,
  "globalDecision": "APPROUVÉ SOUS CONDITIONS"
}
```

**Vérifications:**
- ✅ `status = "UNDER_REVIEW"`
- ✅ `agentId != null` (agent assigné automatiquement)
- ✅ `insuranceRate` présent (venant de l'IA)
- ✅ Fichier sauvegardé dans `uploads/health-reports/`

---

### Test 3 : Vérifier l'assignation de l'agent

**3.1 Vérifier en base de données:**

```sql
SELECT * FROM agent WHERE id = 10;
```

**Résultat attendu:**
- `is_busy = TRUE`
- `current_assigned_request_id = 85`

**3.2 Créer plusieurs demandes pour saturer les agents:**

Répéter le Test 2 trois fois (ou plus) pour occuper tous les agents.

**Résultat attendu:**
- Les 3 agents sont maintenant `is_busy = TRUE`
- La 4ème demande n'aura pas d'agent assigné (`agentId = null`)
- Log warning : "Aucun agent disponible"

---

### Test 4 : Approuver un crédit (en tant qu'agent)

**4.1 Se connecter en tant qu'agent:**

```bash
curl -X POST "http://localhost:8089/forsaPidev/api/auth/signin" \
  -H "Content-Type: application/json" \
  -d '{
    "username": "agent_username",
    "password": "agent_password"
  }'
```

**4.2 Approuver le crédit:**

```bash
curl -X POST "http://localhost:8089/forsaPidev/api/credits/85/approve" \
  -H "Authorization: Bearer <AGENT_TOKEN>"
```

**Résultat attendu:**
```json
{
  "id": 85,
  "status": "APPROVED",
  ...
}
```

**Vérifications:**
- ✅ Status changé en `APPROVED`
- ✅ Échéances générées (vérifier `repayment_schedule`)
- ✅ Gift accumulé (vérifier table `gift`)

---

### Test 5 : Vérifier l'accumulation du gift

**5.1 Requête SQL:**

```sql
SELECT * FROM gift WHERE client_id = 44;
```

**Résultat attendu:**
```
client_id: 44
accumulated_amount: 1500.00  (100000 * 0.015)
threshold: 500.00
awarded: FALSE
```

**5.2 Approuver plusieurs crédits pour atteindre le seuil:**

- Créer un 2ème crédit de 100 000 DT → accumulé = 3 000 DT
- Créer un 3ème crédit de 100 000 DT → accumulé = 4 500 DT

**Après chaque approbation, vérifier:**

```sql
SELECT * FROM gift WHERE client_id = 44;
```

**Quand le seuil est dépassé (≥ 500 DT):**
- ✅ `awarded = TRUE` (temporairement, puis réinitialisé)
- ✅ `awarded_at` rempli
- ✅ `awarded_amount = montant attribué`
- ✅ `accumulated_amount` réinitialisé à 0 (ou surplus si > 500)

---

### Test 6 : Vérifier les échéances générées

**6.1 Récupérer le schedule:**

```bash
curl -X GET "http://localhost:8089/forsaPidev/api/credits/85/schedule" \
  -H "Authorization: Bearer <TOKEN>"
```

**Résultat attendu:**
```json
{
  "creditId": 85,
  "calculationType": "AMORTISSEMENT_CONSTANT",
  "principal": 100000,
  "annualRatePercent": 7.5,
  "durationMonths": 12,
  "periods": [
    {
      "monthNumber": 1,
      "dueDate": "2026-04-02",
      "principalPayment": 8333.33,
      "interestPayment": 625.00,
      "totalPayment": 8958.33,
      "remainingBalance": 91666.67
    },
    ...
  ]
}
```

**Vérifications:**
- ✅ 12 périodes (durationMonths = 12)
- ✅ lineType = "NORMAL" pour toutes
- ✅ Somme des principalPayment ≈ 100 000
- ✅ Dernier remainingBalance ≈ 0

---

### Test 7 : Simuler un retard et tester les pénalités

**7.1 Modifier manuellement une échéance (mettre dueDate dans le passé):**

```sql
UPDATE repayment_schedule 
SET due_date = '2026-02-01', status = 'PENDING'
WHERE credit_request_id = 85 AND id = (
    SELECT MIN(id) FROM repayment_schedule WHERE credit_request_id = 85
);
```

**7.2 Déclencher manuellement le scheduler (ou attendre 1h du matin):**

En Java, créer un endpoint temporaire de test :

```java
@GetMapping("/test/trigger-penalties")
public String triggerPenalties() {
    penaltyService.checkOverduePaymentsAndApplyPenalties();
    return "Penalties checked";
}
```

Ou appeler directement depuis un test unitaire.

**7.3 Vérifier la pénalité appliquée:**

```sql
SELECT * FROM repayment_schedule 
WHERE credit_request_id = 85 AND line_type = 'PENALTY';
```

**Résultat attendu:**
```
line_type: PENALTY
total_amount: 200.00
principal_part: 0.00
interest_part: 0.00
status: PENDING
due_date: 2026-03-02 (date du jour)
```

**7.4 Vérifier que remainingBalance a été mis à jour:**

```sql
SELECT remaining_balance FROM credit_request WHERE id = 85;
```

**Résultat attendu:** `remainingBalance` augmenté de 200 DT

---

### Test 8 : Libérer un agent

**8.1 Endpoint de libération (à créer ou appeler depuis service):**

```java
@PostMapping("/api/agents/{agentId}/release")
public ResponseEntity<?> releaseAgent(@PathVariable Long agentId) {
    agentAssignmentService.releaseAgent(agentId);
    return ResponseEntity.ok("Agent released");
}
```

**8.2 Appeler l'endpoint:**

```bash
curl -X POST "http://localhost:8089/forsaPidev/api/agents/10/release" \
  -H "Authorization: Bearer <ADMIN_TOKEN>"
```

**8.3 Vérifier:**

```sql
SELECT * FROM agent WHERE id = 10;
```

**Résultat attendu:**
- `is_busy = FALSE`
- `current_assigned_request_id = NULL`

---

## 📊 TESTS AVEC POSTMAN

### Collection Postman recommandée

**Créer une collection avec ces requêtes:**

1. **Auth - Signup Client**
   - POST `/api/auth/signup`
   - Body: JSON client data

2. **Auth - Signin Client**
   - POST `/api/auth/signin`
   - Sauvegarder token dans variable {{clientToken}}

3. **Auth - Signin Agent**
   - POST `/api/auth/signin`
   - Sauvegarder token dans variable {{agentToken}}

4. **Credit - Create with Health Report**
   - POST `/api/credits/with-health-report`
   - Auth: Bearer {{clientToken}}
   - Body: form-data (multipart)

5. **Credit - Approve**
   - POST `/api/credits/{{creditId}}/approve`
   - Auth: Bearer {{agentToken}}

6. **Credit - Get Schedule**
   - GET `/api/credits/{{creditId}}/schedule`
   - Auth: Bearer {{clientToken}}

7. **Gift - Get Accumulated** (créer endpoint custom)
   - GET `/api/gifts/client/{{clientId}}`

8. **Agent - Release**
   - POST `/api/agents/{{agentId}}/release`

---

## 🔍 LOGS À SURVEILLER

### Logs de création de crédit avec rapport médical

```
INFO : 🚀 Création d'une demande de crédit avec rapport médical pour l'utilisateur testclient avec montant 100000
INFO : 📡 Appel de l'API Python unifiée pour l'analyse crédit complète...
INFO : 👤 Assignation automatique d'un agent...
INFO : ✅ Demande de crédit {} assignée à l'agent 10 (Agent Smith)
INFO : ✅ Demande de crédit créée avec succès - ID=85
INFO :    📊 Risque fraude : LOW (SAFE)
INFO :    🏥 Assurance : Approuvée - Taux 1.5%
INFO :    🎯 Décision globale : APPROUVÉ SOUS CONDITIONS
```

### Logs d'approbation et gift

```
INFO : Validation de la demande de crédit ID=85
INFO : Crédit ID=85 validé avec succès
INFO : 🎁 Accumulation du gift pour le crédit ID=85
INFO : Accumulation gift pour client 44 : capital=100000, increment=1500.00
INFO : Gift accumulé pour client 44 : montant total=1500.00, seuil=500.00
INFO : ✅ Seuil atteint pour client 44 ! Montant accumulé : 1500.00
INFO : 🎁 Gift attribué au client 44 : montant=1500.00 DT
```

### Logs de pénalités

```
INFO : 🔍 Début de la vérification des retards de paiement...
INFO : Nombre d'échéances en retard trouvées : 1
WARN : ⚠️ Retard de 30 jours détecté pour échéance 123 du crédit 85
WARN : 💰 Pénalité de 200.00 DT appliquée au crédit 85 - Nouveau solde : 100200.00
INFO : ✅ Vérification des retards terminée
```

---

## ⚠️ ERREURS COURANTES ET SOLUTIONS

### Erreur 1 : "Service IA indisponible"

**Log:**
```
ERROR : Impossible de joindre le service IA sur http://localhost:8000/predict-with-report
```

**Solution:**
- Vérifier que l'API IA est démarrée : `python run.py`
- Tester manuellement : `curl http://localhost:8000/health`
- Vérifier le timeout dans `application.properties`

---

### Erreur 2 : "Aucun agent disponible"

**Log:**
```
WARN : Aucun agent disponible pour assigner la demande de crédit 85
```

**Solution:**
- Vérifier qu'il existe des agents : `SELECT * FROM agent WHERE is_active = TRUE AND is_busy = FALSE`
- Créer des agents si nécessaire
- Libérer un agent occupé

---

### Erreur 3 : "Unauthorized error: Full authentication is required"

**Solution:**
- Vérifier que le token JWT est valide
- Vérifier que le header Authorization est bien présent : `Authorization: Bearer <token>`
- Vérifier que la clé secrète JWT est identique entre génération et validation

---

### Erreur 4 : Table 'gift' doesn't exist

**Solution:**
- Exécuter le script SQL de création de la table gift
- Redémarrer l'application Spring Boot

---

## ✅ VALIDATION FINALE

### Checklist complète

- [ ] Projet compile sans erreur : `./mvnw.cmd clean compile`
- [ ] Tables créées en base (agent, gift, line_type)
- [ ] Agents insérés en base (minimum 2-3)
- [ ] API IA démarrée et accessible
- [ ] Application Spring Boot démarrée (port 8089)
- [ ] Authentification fonctionne (obtention token)
- [ ] Création crédit avec rapport médical OK
- [ ] Agent assigné automatiquement
- [ ] Approbation crédit génère schedule
- [ ] Gift accumulé après approbation
- [ ] Gift attribué automatiquement >= 500 DT
- [ ] Pénalité appliquée sur retard
- [ ] Libération agent fonctionne

---

## 📈 RÉSULTATS ATTENDUS

**Après tous les tests, vous devriez avoir:**

1. **En base `credit_request`:**
   - Plusieurs demandes avec status APPROVED
   - Champs `agentId` remplis
   - Champs `insuranceRate`, `insuranceAmount` remplis

2. **En base `agent`:**
   - Agents avec `is_busy = TRUE` (si assignés)
   - `current_assigned_request_id` remplis pour agents occupés

3. **En base `gift`:**
   - Enregistrements pour chaque client ayant des crédits approuvés
   - `accumulated_amount` croissant
   - Historique d'attributions (awarded = TRUE)

4. **En base `repayment_schedule`:**
   - Échéances avec `line_type = NORMAL`
   - Lignes de pénalité avec `line_type = PENALTY` et `total_amount = 200.00`

5. **Fichiers uploads:**
   - Rapports médicaux dans `uploads/health-reports/`

---

## 🎯 CONCLUSION

Si tous les tests passent :

✅ **Le système est fonctionnel et production-ready**

Prochaines étapes :
1. Tests automatisés (JUnit, Mockito)
2. Tests de charge
3. Documentation Swagger complète
4. Monitoring (Prometheus, Grafana)
5. CI/CD Pipeline

---

**Bonne chance avec les tests ! 🚀**

