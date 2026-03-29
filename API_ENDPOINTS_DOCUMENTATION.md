# 📡 API ENDPOINTS - DOCUMENTATION COMPLÈTE

## Base URL
```
http://localhost:8089/forsaPidev
```

---

## 🔐 AUTHENTIFICATION

### 1. Inscription (Signup)
```http
POST /api/auth/signup
Content-Type: application/json

Body:
{
  "username": "testuser",
  "email": "test@example.com",
  "password": "Test@1234",
  "role": "CLIENT" | "AGENT" | "ADMIN"
}

Response: 201 Created
{
  "message": "User registered successfully!"
}
```

### 2. Connexion (Signin)
```http
POST /api/auth/signin
Content-Type: application/json

Body:
{
  "username": "testuser",
  "password": "Test@1234"
}

Response: 200 OK
{
  "token": "eyJhbGciOiJIUzUxMiJ9...",
  "type": "Bearer",
  "id": 44,
  "username": "testuser",
  "email": "test@example.com",
  "roles": ["ROLE_CLIENT"]
}
```

---

## 💳 GESTION DES CRÉDITS

### 3. Créer une demande de crédit avec rapport médical
```http
POST /api/credits/with-health-report
Authorization: Bearer <token>
Content-Type: multipart/form-data

Form Data:
- amountRequested: 100000 (BigDecimal)
- durationMonths: 12 (Integer)
- typeCalcul: "ANNUITE_CONSTANTE" | "AMORTISSEMENT_CONSTANT"
- healthReport: <file.pdf> (MultipartFile)

Response: 201 Created
{
  "id": 85,
  "amountRequested": 100000,
  "interestRate": 7.5,
  "durationMonths": 12,
  "status": "UNDER_REVIEW",
  "requestDate": "2026-03-02T00:00:00",
  "agentId": 10,
  "user": {
    "id": 44,
    "username": "testuser"
  },
  "typeCalcul": "AMORTISSEMENT_CONSTANT",
  "remainingBalance": 0,
  "paidInstallments": 0,
  "isRisky": false,
  "riskLevel": "LOW",
  "scoredAt": "2026-03-02T00:00:00",
  "healthReportPath": "uploads/health-reports/health_report_44_85_xxx.pdf",
  "insuranceRate": 1.5,
  "insuranceAmount": 1500.00,
  "insuranceIsReject": false,
  "insuranceRating": "NORMAL",
  "globalDecision": "APPROUVÉ SOUS CONDITIONS",
  "globalPdfPath": "reports/rapport_global_xxx.pdf"
}
```

### 4. Récupérer toutes les demandes de crédit
```http
GET /api/credits
Authorization: Bearer <token>

Response: 200 OK
[
  {
    "id": 85,
    "amountRequested": 100000,
    "status": "UNDER_REVIEW",
    ...
  },
  ...
]
```

### 5. Récupérer une demande de crédit par ID
```http
GET /api/credits/{id}
Authorization: Bearer <token>

Response: 200 OK
{
  "id": 85,
  ...
}
```

### 6. Approuver une demande de crédit (AGENT/ADMIN)
```http
POST /api/credits/{id}/approve
Authorization: Bearer <agent_token>

Response: 200 OK
{
  "id": 85,
  "status": "APPROVED",
  ...
}
```

**Note:** Cette action déclenche :
- Génération du tableau d'amortissement
- Accumulation du gift (1.5% du capital)
- Vérification du seuil gift (≥ 500 DT → attribution automatique)

### 7. Rejeter une demande de crédit (AGENT/ADMIN)
```http
POST /api/credits/{id}/reject
Authorization: Bearer <agent_token>
Content-Type: application/json

Body:
{
  "reason": "Profil à risque élevé"
}

Response: 200 OK
{
  "id": 85,
  "status": "DEFAULTED",
  ...
}
```

### 8. Simuler un tableau d'amortissement (sans créer le crédit)
```http
GET /api/credits/simulate?principal=100000&rate=7.5&duration=12&type=AMORTISSEMENT_CONSTANT
Authorization: Bearer <token>

Response: 200 OK
{
  "calculationType": "AMORTISSEMENT_CONSTANT",
  "principal": 100000,
  "annualRatePercent": 7.5,
  "durationMonths": 12,
  "totalInterest": 4875.00,
  "totalAmount": 104875.00,
  "periods": [
    {
      "monthNumber": 1,
      "principalPayment": 8333.33,
      "interestPayment": 625.00,
      "totalPayment": 8958.33,
      "remainingBalance": 91666.67
    },
    ...
  ]
}
```

### 9. Récupérer le tableau d'amortissement d'un crédit existant
```http
GET /api/credits/{id}/schedule
Authorization: Bearer <token>

Response: 200 OK
{
  "creditId": 85,
  "calculationType": "AMORTISSEMENT_CONSTANT",
  "principal": 100000,
  "annualRatePercent": 7.5,
  "durationMonths": 12,
  "totalInterest": 4875.00,
  "totalAmount": 104875.00,
  "periods": [...]
}
```

### 10. Récupérer les crédits en attente (AGENT/ADMIN)
```http
GET /api/credits/pending
Authorization: Bearer <agent_token>

Response: 200 OK
[
  {
    "id": 85,
    "status": "SUBMITTED",
    ...
  },
  ...
]
```

---

## 📅 GESTION DES ÉCHÉANCES (REPAYMENT SCHEDULE)

### 11. Récupérer les échéances d'un crédit
```http
GET /api/repayments/credit/{creditId}
Authorization: Bearer <token>

Response: 200 OK
[
  {
    "id": 1,
    "dueDate": "2026-04-02",
    "paidDate": null,
    "totalAmount": 8958.33,
    "principalPart": 8333.33,
    "interestPart": 625.00,
    "remainingBalance": 91666.67,
    "status": "PENDING",
    "lineType": "NORMAL"
  },
  ...
]
```

### 12. Marquer une échéance comme payée
```http
PATCH /api/repayments/{id}/pay?amount=8958.33
Authorization: Bearer <token>

Response: 200 OK
{
  "id": 1,
  "status": "PAID",
  "paidDate": "2026-03-02",
  ...
}
```

**Note:** Cette action peut déclencher :
- Mise à jour du `paidInstallments` du crédit
- Mise à jour du `remainingBalance`
- Si toutes les échéances payées → status crédit = "REPAID"

---

## 👤 GESTION DES AGENTS (Nouveaux endpoints)

### 13. Créer un agent
```http
POST /api/agents
Authorization: Bearer <admin_token>
Content-Type: application/json

Body:
{
  "userId": 10,
  "fullName": "Agent Smith"
}

Response: 201 Created
{
  "id": 1,
  "user": { "id": 10, ... },
  "fullName": "Agent Smith",
  "isBusy": false,
  "currentAssignedRequestId": null,
  "isActive": true,
  "createdAt": "2026-03-02T00:00:00"
}
```

### 14. Récupérer tous les agents disponibles
```http
GET /api/agents/available
Authorization: Bearer <admin_token>

Response: 200 OK
[
  {
    "id": 1,
    "fullName": "Agent Smith",
    "isBusy": false,
    "isActive": true
  },
  ...
]
```

### 15. Récupérer tous les agents occupés
```http
GET /api/agents/busy
Authorization: Bearer <admin_token>

Response: 200 OK
[
  {
    "id": 2,
    "fullName": "Agent Johnson",
    "isBusy": true,
    "currentAssignedRequestId": 85
  },
  ...
]
```

### 16. Libérer un agent (marquer comme disponible)
```http
POST /api/agents/{agentId}/release
Authorization: Bearer <admin_token>

Response: 200 OK
{
  "message": "Agent 1 libéré avec succès"
}
```

### 17. Activer/Désactiver un agent
```http
PATCH /api/agents/{agentId}/toggle?active=false
Authorization: Bearer <admin_token>

Response: 200 OK
{
  "id": 1,
  "isActive": false,
  ...
}
```

---

## 🎁 GESTION DES GIFTS (Nouveaux endpoints)

### 18. Récupérer le gift d'un client
```http
GET /api/gifts/client/{clientId}
Authorization: Bearer <token>

Response: 200 OK
{
  "id": 1,
  "clientId": 44,
  "accumulatedAmount": 1500.00,
  "threshold": 500.00,
  "awarded": false,
  "createdAt": "2026-03-02T00:00:00",
  "updatedAt": "2026-03-02T01:00:00"
}
```

### 19. Récupérer tous les gifts en attente (≥ seuil)
```http
GET /api/gifts/pending
Authorization: Bearer <admin_token>

Response: 200 OK
[
  {
    "id": 1,
    "clientId": 44,
    "accumulatedAmount": 1500.00,
    "threshold": 500.00,
    "awarded": false
  },
  ...
]
```

### 20. Traiter manuellement un gift (attribution)
```http
POST /api/gifts/{giftId}/award
Authorization: Bearer <admin_token>

Response: 200 OK
{
  "id": 1,
  "clientId": 44,
  "accumulatedAmount": 0.00,
  "awarded": true,
  "awardedAt": "2026-03-02T02:00:00",
  "awardedAmount": 1500.00
}
```

### 21. Traiter tous les gifts en attente
```http
POST /api/gifts/process-all
Authorization: Bearer <admin_token>

Response: 200 OK
{
  "message": "3 gifts traités avec succès"
}
```

---

## 💰 GESTION DES PÉNALITÉS (Nouveaux endpoints)

### 22. Récupérer les pénalités d'un crédit
```http
GET /api/penalties/credit/{creditId}
Authorization: Bearer <token>

Response: 200 OK
[
  {
    "id": 123,
    "dueDate": "2026-03-02",
    "totalAmount": 200.00,
    "principalPart": 0.00,
    "interestPart": 0.00,
    "remainingBalance": 100200.00,
    "status": "PENDING",
    "lineType": "PENALTY"
  }
]
```

### 23. Appliquer manuellement une pénalité
```http
POST /api/penalties/credit/{creditId}/apply
Authorization: Bearer <admin_token>

Response: 200 OK
{
  "id": 123,
  "totalAmount": 200.00,
  "lineType": "PENALTY",
  ...
}
```

### 24. Déclencher manuellement la vérification des retards (test)
```http
POST /api/penalties/check-all
Authorization: Bearer <admin_token>

Response: 200 OK
{
  "message": "Vérification terminée : 5 pénalités appliquées"
}
```

**Note:** En production, ce processus est automatique (scheduler quotidien à 1h du matin).

---

## 📊 STATISTIQUES ET RAPPORTS

### 25. Statistiques globales des crédits
```http
GET /api/credits/stats
Authorization: Bearer <admin_token>

Response: 200 OK
{
  "totalCredits": 150,
  "approvedCredits": 120,
  "pendingCredits": 20,
  "rejectedCredits": 10,
  "totalAmountRequested": 15000000.00,
  "totalAmountApproved": 12000000.00
}
```

### 26. Statistiques des agents
```http
GET /api/agents/stats
Authorization: Bearer <admin_token>

Response: 200 OK
{
  "totalAgents": 10,
  "availableAgents": 5,
  "busyAgents": 5,
  "averageCreditsPerAgent": 12.5
}
```

### 27. Statistiques des gifts
```http
GET /api/gifts/stats
Authorization: Bearer <admin_token>

Response: 200 OK
{
  "totalGiftsAwarded": 25,
  "totalAmountAwarded": 15000.00,
  "pendingGifts": 5,
  "pendingAmount": 3500.00
}
```

### 28. Statistiques des pénalités
```http
GET /api/penalties/stats
Authorization: Bearer <admin_token>

Response: 200 OK
{
  "totalPenalties": 8,
  "totalAmountPenalties": 1600.00,
  "creditsWithPenalties": 8
}
```

---

## 🔧 ENDPOINTS DE DIAGNOSTIC (DEV ONLY)

### 29. Health Check
```http
GET /actuator/health

Response: 200 OK
{
  "status": "UP"
}
```

### 30. Info Application
```http
GET /actuator/info

Response: 200 OK
{
  "app": {
    "name": "ForsaPidev",
    "version": "1.0.0"
  }
}
```

---

## 📋 CODES D'ERREUR

### 400 Bad Request
- Données manquantes ou invalides
- Format de fichier non supporté

### 401 Unauthorized
- Token JWT manquant ou invalide
- Token expiré

### 403 Forbidden
- Permissions insuffisantes (ex: CLIENT tente d'approuver un crédit)

### 404 Not Found
- Ressource inexistante (crédit, agent, etc.)

### 409 Conflict
- État invalide (ex: tenter d'approuver un crédit déjà approuvé)

### 500 Internal Server Error
- Erreur serveur (consulter les logs)

### 503 Service Unavailable
- Service IA indisponible

---

## 🔒 MATRICE DES PERMISSIONS

| Endpoint | CLIENT | AGENT | ADMIN |
|----------|--------|-------|-------|
| POST /api/credits/with-health-report | ✅ | ✅ | ✅ |
| GET /api/credits | ✅ | ✅ | ✅ |
| GET /api/credits/{id} | ✅ (owner) | ✅ | ✅ |
| POST /api/credits/{id}/approve | ❌ | ✅ | ✅ |
| POST /api/credits/{id}/reject | ❌ | ✅ | ✅ |
| GET /api/credits/pending | ❌ | ✅ | ✅ |
| POST /api/agents | ❌ | ❌ | ✅ |
| POST /api/agents/{id}/release | ❌ | ❌ | ✅ |
| GET /api/gifts/client/{id} | ✅ (owner) | ✅ | ✅ |
| POST /api/gifts/process-all | ❌ | ❌ | ✅ |
| POST /api/penalties/check-all | ❌ | ❌ | ✅ |

---

## 💡 EXEMPLES DE FLUX COMPLETS

### Flux 1 : Client crée une demande → Agent approuve

1. **Client se connecte**
   ```
   POST /api/auth/signin
   ```

2. **Client crée une demande avec rapport médical**
   ```
   POST /api/credits/with-health-report
   ```

3. **Système assigne automatiquement un agent**
   - Agent reçoit notification (à implémenter)

4. **Agent se connecte**
   ```
   POST /api/auth/signin
   ```

5. **Agent consulte les demandes en attente**
   ```
   GET /api/credits/pending
   ```

6. **Agent approuve la demande**
   ```
   POST /api/credits/{id}/approve
   ```

7. **Système génère automatiquement:**
   - Tableau d'amortissement
   - Accumulation du gift
   - Attribution si seuil atteint

8. **Client consulte son tableau d'amortissement**
   ```
   GET /api/credits/{id}/schedule
   ```

---

### Flux 2 : Paiement d'une échéance

1. **Client consulte ses échéances**
   ```
   GET /api/repayments/credit/{creditId}
   ```

2. **Client effectue le paiement (via PSP)**
   - Intégration externe (Stripe, PayPal, etc.)

3. **Système marque l'échéance comme payée**
   ```
   PATCH /api/repayments/{id}/pay
   ```

4. **Système met à jour automatiquement:**
   - `paidInstallments` du crédit
   - `remainingBalance`
   - Si toutes payées → status = "REPAID"

---

### Flux 3 : Retard → Pénalité automatique

1. **Client ne paie pas avant dueDate**

2. **Scheduler quotidien s'exécute (1h du matin)**
   ```
   @Scheduled checkOverduePaymentsAndApplyPenalties()
   ```

3. **Système détecte le retard**
   - dueDate < aujourd'hui
   - status != PAID

4. **Système applique automatiquement la pénalité**
   - Crée ligne PENALTY (200 DT)
   - Met à jour remainingBalance

5. **Client consulte ses échéances et voit la pénalité**
   ```
   GET /api/repayments/credit/{creditId}
   ```

---

## 🎯 CONCLUSION

Cette API offre :

✅ **Gestion complète des crédits** avec workflow IA  
✅ **Assignation automatique des agents**  
✅ **Système de gifts** (1.5% accumulation)  
✅ **Pénalités automatiques** (200 DT par retard)  
✅ **Calculs d'échéances** (2 stratégies)  
✅ **Sécurité JWT** robuste  

**Documentation Swagger interactive:**  
http://localhost:8089/forsaPidev/swagger-ui.html

---

**Version:** 1.0.0  
**Dernière mise à jour:** 2026-03-02

