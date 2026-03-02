# 📡 Exemple Concret - Communication Spring ↔ IA

## 🎯 Flux complet de scoring

Voici exactement ce qui se passe quand un client fait une demande de crédit.

---

## 1️⃣ Le client envoie une demande

**Requête vers Spring Boot :**
```http
POST http://localhost:8089/forsaPidev/api/credits
Content-Type: application/json

{
  "amountRequested": 15000,
  "durationMonths": 24,
  "typeCalcul": "AMORTISSEMENT_CONSTANT"
}
```

---

## 2️⃣ Spring prépare les features et appelle votre IA

**Spring construit automatiquement ce JSON :**

```json
{
  "avg_delay_days": 1.7656729696616313,
  "payment_instability": 0.09726366622798913,
  "credit_utilization": 0.5814332158308677,
  "monthly_transaction_count": 8,
  "transaction_amount_std": 201.17363697219358,
  "high_risk_country_transaction": 0,
  "unusual_night_transaction": 0,
  "address_changed": 0,
  "phone_changed": 0,
  "email_changed": 0,
  "country_changed": 0,
  "income_change_percentage": 10.274386518599968,
  "employment_changed": 0
}
```

**Spring envoie ce JSON à :**
```
POST http://localhost:8000/predict
Content-Type: application/json
```

---

## 3️⃣ Votre modèle IA analyse et retourne le résultat

**Votre modèle retourne :**

```json
{
  "score": 0.85,
  "risky": true,
  "risk_level": "HIGH"
}
```

**Interprétation :**
- `score` : 0.85 = 85% de probabilité de risque
- `risky` : true = Client considéré comme risqué
- `risk_level` : HIGH = Niveau de risque élevé

---

## 4️⃣ Spring utilise directement ces valeurs

**Spring met à jour la demande de crédit :**

```java
creditRequest.setRiskScore(0.85);           // Score de l'IA
creditRequest.setIsRisky(true);             // Décision de l'IA
creditRequest.setRiskLevel(RiskLevel.HIGH); // Niveau de l'IA
creditRequest.setScoredAt(LocalDateTime.now());
creditRequest.setStatus(CreditStatus.UNDER_REVIEW);
```

---

## 5️⃣ Spring retourne la réponse au client

**Réponse JSON :**

```json
{
  "id": 1,
  "amountRequested": 15000,
  "interestRate": 5.5,
  "durationMonths": 24,
  "status": "UNDER_REVIEW",
  "requestDate": "2026-02-28T02:30:00",
  "typeCalcul": "AMORTISSEMENT_CONSTANT",
  "riskScore": 0.85,
  "isRisky": true,
  "riskLevel": "HIGH",
  "scoredAt": "2026-02-28T02:30:15"
}
```

---

## 6️⃣ L'agent consulte la demande

**Requête agent :**
```http
GET http://localhost:8089/forsaPidev/api/credits/pending
```

**Réponse :**
```json
[
  {
    "id": 1,
    "amountRequested": 15000,
    "status": "UNDER_REVIEW",
    "riskScore": 0.85,
    "isRisky": true,
    "riskLevel": "HIGH",
    "scoredAt": "2026-02-28T02:30:15"
  }
]
```

**L'agent voit :**
- 💰 Montant demandé : 15 000
- ⚠️ Score IA : 0.85 (85% de risque)
- ❌ Client risqué : OUI
- 🔴 Niveau : HAUT

**L'agent décide :**
- Soit il REJETTE : `POST /api/credits/1/reject`
- Soit il APPROUVE quand même (décision humaine) : `POST /api/credits/1/approve`

---

## 🔍 Exemple avec un client PEU risqué

### Demande
```json
{
  "amountRequested": 5000,
  "durationMonths": 12
}
```

### Features envoyées à l'IA
```json
{
  "avg_delay_days": 0.2,
  "payment_instability": 0.05,
  "credit_utilization": 0.3,
  "monthly_transaction_count": 15,
  "transaction_amount_std": 50.0,
  "high_risk_country_transaction": 0,
  "unusual_night_transaction": 0,
  "address_changed": 0,
  "phone_changed": 0,
  "email_changed": 0,
  "country_changed": 0,
  "income_change_percentage": 0.0,
  "employment_changed": 0
}
```

### Réponse de l'IA
```json
{
  "score": 0.15,
  "risky": false,
  "risk_level": "LOW"
}
```

### Réponse finale au client
```json
{
  "id": 2,
  "amountRequested": 5000,
  "status": "UNDER_REVIEW",
  "riskScore": 0.15,
  "isRisky": false,
  "riskLevel": "LOW"
}
```

**L'agent voit un bon dossier et approuve rapidement.**

---

## 🧪 Comment tester avec POSTMAN

### Collection Postman complète

#### 1. Créer une demande (avec scoring auto)
```
POST http://localhost:8089/forsaPidev/api/credits
Headers: Content-Type: application/json
Body:
{
  "amountRequested": 15000,
  "durationMonths": 24,
  "typeCalcul": "AMORTISSEMENT_CONSTANT"
}
```

#### 2. Lister les crédits en attente (vue agent)
```
GET http://localhost:8089/forsaPidev/api/credits/pending
```

#### 3. Voir un crédit spécifique
```
GET http://localhost:8089/forsaPidev/api/credits/1
```

#### 4. Approuver (agent)
```
POST http://localhost:8089/forsaPidev/api/credits/1/approve
```

#### 5. Rejeter (agent)
```
POST http://localhost:8089/forsaPidev/api/credits/2/reject
Headers: Content-Type: application/json
Body:
{
  "reason": "Score trop élevé, risque inacceptable"
}
```

---

## 📊 Logs attendus

### Dans Spring Boot (console IntelliJ)

```
2026-02-28T02:30:10 INFO  - Création d'une nouvelle demande de crédit pour un montant de 15000
2026-02-28T02:30:11 INFO  - Lancement du scoring IA pour la demande de crédit ID=1
2026-02-28T02:30:11 DEBUG - Features construites pour crédit ID=1
2026-02-28T02:30:12 INFO  - Appel du service IA de scoring sur http://localhost:8000/predict
2026-02-28T02:30:13 INFO  - Score IA reçu avec succès : score=0.85, risky=true
2026-02-28T02:30:13 INFO  - Scoring terminé pour crédit ID=1 : score=0.85, risky=true, level=HIGH
2026-02-28T02:30:13 INFO  - Demande de crédit créée avec succès - ID=1, Score=0.85, Risque=HIGH
```

### Dans votre service IA Python (uvicorn)

```
INFO: 127.0.0.1:58234 - "POST /predict HTTP/1.1" 200 OK
```

---

## ⚠️ En cas de problème

### Service IA down

**Logs Spring :**
```
ERROR - Impossible de joindre le service IA sur http://localhost:8000/predict : Connection refused
WARN  - Le scoring IA a échoué pour la demande ID=1 - Demande laissée en statut SUBMITTED
```

**Comportement :**
- La demande est créée quand même
- Status = SUBMITTED (au lieu de UNDER_REVIEW)
- `riskScore` = null
- L'agent verra la demande mais sans score IA
- Il devra décider manuellement

---

## 🎯 Points clés à retenir

1. ✅ Spring envoie **13 features** exactement comme votre modèle les attend
2. ✅ Votre modèle retourne **score + risky + risk_level**
3. ✅ Spring utilise **directement** ces valeurs sans les recalculer
4. ✅ Les agents voient le score et décident d'approuver ou rejeter
5. ✅ Si l'IA est down, la demande est quand même créée (fallback)

---

**🎉 Le système est prêt ! Il ne reste plus qu'à tester avec votre vrai modèle IA ! 🎉**

