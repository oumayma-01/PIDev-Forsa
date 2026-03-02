# ✅ CHECKLIST PROCHAINES ÉTAPES - Scoring IA

## 🎯 Ce qui a été fait

- ✅ Architecture complète du scoring IA implémentée
- ✅ Tous les composants créés (DTOs, services, client, contrôleurs)
- ✅ Base de données mise à jour (4 nouvelles colonnes)
- ✅ Compilation réussie (BUILD SUCCESS)
- ✅ Documentation complète (3 fichiers)
- ✅ Gestion des erreurs robuste
- ✅ 3 nouveaux endpoints REST pour les agents

---

## 📋 À FAIRE MAINTENANT (dans l'ordre)

### 1. ⚠️ URGENT : Arrêter l'instance Spring Boot qui tourne

Le port 8089 est occupé. Deux solutions :

**Option A : Depuis IntelliJ**
- Stop (carré rouge) dans la fenêtre Run

**Option B : Depuis PowerShell**
```powershell
# Trouver le PID du processus qui utilise le port 8089
netstat -ano | findstr :8089

# Tuer le processus (remplacer 12345 par le PID trouvé)
taskkill /PID 12345 /F
```

---

### 2. 🐍 Lancer votre service IA Python

```bash
# Dans le dossier de votre projet IA
cd chemin\vers\projet-ia

# Activer l'environnement virtuel
.\venv\Scripts\Activate.ps1   # PowerShell
# OU
source venv/Scripts/activate   # Git Bash

# Lancer le serveur
uvicorn main:app --reload --port 8000
# OU
python app.py
```

**Vérifier** que vous voyez :
```
INFO: Uvicorn running on http://127.0.0.1:8000
```

**Tester** que l'IA répond :
```bash
curl -X POST http://localhost:8000/predict \
  -H "Content-Type: application/json" \
  -d '{"avg_delay_days":1.7,"payment_instability":0.1,"credit_utilization":0.5,"monthly_transaction_count":8,"transaction_amount_std":200,"high_risk_country_transaction":0,"unusual_night_transaction":0,"address_changed":0,"phone_changed":0,"email_changed":0,"country_changed":0,"income_change_percentage":10}'
```

---

### 3. 🚀 Lancer Spring Boot

**Dans IntelliJ :**
- Run `ForsaPidevApplication`

**OU en terminal :**
```bash
.\mvnw.cmd spring-boot:run
```

**Vérifier** que vous voyez :
```
Tomcat started on port(s): 8089 (http) with context path '/forsaPidev'
Started ForsaPidevApplication in X seconds
```

---

### 4. 🧪 Tester le flux complet

**4.1 - Créer une demande de crédit (avec scoring automatique)**

Dans Postman ou Insomnia :

```http
POST http://localhost:8089/forsaPidev/api/credits
Content-Type: application/json

{
  "amountRequested": 15000,
  "durationMonths": 24,
  "typeCalcul": "AMORTISSEMENT_CONSTANT"
}
```

**Résultat attendu :**
```json
{
  "id": 1,
  "status": "UNDER_REVIEW",
  "riskScore": 0.XX,
  "riskLevel": "LOW" ou "MEDIUM" ou "HIGH",
  "scoredAt": "2026-02-28T..."
}
```

**4.2 - Consulter les crédits en attente (vue agent)**

```http
GET http://localhost:8089/forsaPidev/api/credits/pending
```

**4.3 - Approuver le crédit**

```http
POST http://localhost:8089/forsaPidev/api/credits/1/approve
```

**4.4 - Tester le rejet**

Créer un nouveau crédit, puis :
```http
POST http://localhost:8089/forsaPidev/api/credits/2/reject
Content-Type: application/json

{
  "reason": "Test de rejet"
}
```

---

### 5. 📊 Vérifier dans la base de données

Connectez-vous à MySQL et vérifiez :

```sql
USE ForsaBD;

-- Voir les nouvelles colonnes
DESCRIBE credit_request;

-- Voir les données scorées
SELECT 
  id, 
  amount_requested, 
  status, 
  risk_score, 
  risk_level, 
  is_risky, 
  scored_at 
FROM credit_request;
```

---

### 6. ⚠️ PRIORITÉ : Implémenter le calcul des vraies features

**Problème actuel :**  
Le fichier `CreditScoringService.java` utilise des **valeurs par défaut** pour les features.

**Fichier à modifier :**
```
src/main/java/org/example/forsapidev/Services/scoring/CreditScoringService.java
Méthode : buildFeatures(CreditRequest creditRequest)
Ligne : ~75
```

**Ce qu'il faut faire :**

Remplacer les valeurs fictives par de vrais calculs basés sur :

- **Historique de paiements** (si disponible) :
  - `avg_delay_days` : moyenne des jours de retard
  - `payment_instability` : écart-type des retards

- **Wallet / Transactions** :
  - `credit_utilization` : montant utilisé / limite autorisée
  - `monthly_transaction_count` : nombre de transactions du dernier mois
  - `transaction_amount_std` : écart-type des montants de transactions

- **Profil client** :
  - `address_changed`, `phone_changed`, `email_changed`, `country_changed` : 0 ou 1
  - `income_change_percentage` : % de variation du revenu

- **Sécurité** :
  - `high_risk_country_transaction` : transactions dans pays à risque
  - `unusual_night_transaction` : transactions nocturnes inhabituelles

**Exemple de code à adapter :**
```java
private ScoringRequestDto buildFeatures(CreditRequest creditRequest) {
    ScoringRequestDto features = new ScoringRequestDto();
    
    // TODO: Calculer à partir de l'historique réel
    // Par exemple, si vous avez une entité User liée :
    // User client = userRepository.findById(creditRequest.getUserId());
    // features.setAvgDelayDays(calculateAverageDelay(client));
    
    // Pour l'instant :
    features.setAvgDelayDays(0.0);  // À REMPLACER
    features.setPaymentInstability(0.0);  // À REMPLACER
    // ... etc
    
    return features;
}
```

---

## 📚 Documentation de référence

| Fichier | Contenu |
|---------|---------|
| **ARCHITECTURE_SCORING_IA.md** | Architecture complète, contrat API, configuration |
| **GUIDE_TEST_SCORING_IA.md** | Guide de test étape par étape, dépannage |
| **RECAP_SCORING_IA.md** | Récapitulatif technique détaillé |
| **SUCCESS_SCORING_IA.md** | Preuve de succès, statistiques, évolutions |

---

## 🎯 Checklist de validation finale

Cochez au fur et à mesure :

- [ ] Service IA Python démarre sur port 8000
- [ ] Service IA répond correctement à `/predict`
- [ ] Spring Boot démarre sans erreur
- [ ] Nouvelles colonnes présentes dans la table `credit_request`
- [ ] Création d'un crédit → score IA calculé et sauvegardé
- [ ] Endpoint `/api/credits/pending` retourne les crédits avec scores
- [ ] Approbation d'un crédit fonctionne
- [ ] Rejet d'un crédit fonctionne
- [ ] Logs montrent les appels IA et les scores reçus
- [ ] En cas de service IA down, le crédit est créé en SUBMITTED

---

## 🛠️ En cas de problème

### Port 8089 occupé
```powershell
netstat -ano | findstr :8089
taskkill /PID <PID> /F
```

### MySQL ne démarre pas
- Vérifier XAMPP/WAMP
- Ou démarrer le service Windows "MySQL"

### Service IA ne répond pas
- Vérifier que le serveur Python tourne
- Vérifier le port (doit être 8000)
- Tester avec `curl` ou Postman

### Erreur de compilation
```bash
.\mvnw.cmd clean compile
```

### Score reste null après création
- Vérifier les logs Spring (rechercher "scoring")
- Vérifier que `ai.scoring.enabled=true`
- Vérifier que le service IA est accessible

---

## 🚀 Prochaines étapes (après validation)

1. ✅ Implémenter le calcul des vraies features
2. ✅ Tests avec données réelles variées
3. ✅ Ajuster le seuil de risque (`ai.scoring.risk-threshold`)
4. ✅ Ajouter un endpoint de re-scoring manuel
5. ✅ Tests unitaires et d'intégration
6. 🐳 Préparer Docker Compose (IA + Spring + MySQL)

---

**🎉 Tout est prêt ! Il ne reste plus qu'à lancer les services et tester ! 🎉**

