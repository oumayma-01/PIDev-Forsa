# Guide de Test - Intégration Scoring IA

## 🎯 Objectif

Tester le flux complet d'intégration entre Spring Boot et le service IA de scoring, en local, sans Docker.

---

## ✅ Prérequis

### 1. Service IA fonctionnel

Le service IA doit être démarré et accessible sur `http://localhost:8000/predict`.

**Vérification :**

```bash
# Avec curl (Git Bash)
curl -X POST http://localhost:8000/predict \
  -H "Content-Type: application/json" \
  -d '{
    "avg_delay_days": 1.76,
    "payment_instability": 0.097,
    "credit_utilization": 0.58,
    "monthly_transaction_count": 8,
    "transaction_amount_std": 201.17,
    "high_risk_country_transaction": 0,
    "unusual_night_transaction": 0,
    "address_changed": 0,
    "phone_changed": 0,
    "email_changed": 0,
    "country_changed": 0,
    "income_change_percentage": 10.27
  }'
```

**Réponse attendue :**
```json
{
  "score": 0.xx,
  "risky": false
}
```

Si cette commande ne fonctionne pas, **ne pas continuer** - corriger d'abord le service IA.

### 2. Base de données MySQL

- MySQL doit être démarré (XAMPP, WAMP, service Windows, etc.)
- Base `ForsaBD` créée (ou auto-créée par Spring)
- User `root` sans mot de passe (ou adapter `application.properties`)

### 3. Spring Boot

- Projet compilé sans erreurs
- Configuration dans `application.properties` correcte (notamment URL IA)

---

## 🚀 Procédure de test

### Étape 1 : Lancer le service IA

Dans le terminal du projet IA (Python) :

```bash
# Activer l'environnement virtuel
# Sous Windows PowerShell :
.\venv\Scripts\Activate.ps1

# Ou sous Git Bash :
source venv/Scripts/activate

# Lancer le serveur (exemple avec uvicorn pour FastAPI)
uvicorn main:app --reload --port 8000

# Ou avec Flask
python app.py
```

**Vérifier dans les logs :** 
```
INFO: Uvicorn running on http://127.0.0.1:8000
```

Laisser ce terminal ouvert.

---

### Étape 2 : Lancer Spring Boot

Dans IntelliJ IDEA :
1. Ouvrir la classe `ForsaPidevApplication`
2. Cliquer sur le bouton ▶️ Run

Ou en ligne de commande :
```bash
mvnw.cmd spring-boot:run
```

**Vérifier dans les logs :**
```
Tomcat started on port(s): 8089 (http) with context path '/forsaPidev'
Started ForsaPidevApplication in X seconds
```

L'application est accessible sur : `http://localhost:8089/forsaPidev`

---

### Étape 3 : Tester la création d'une demande de crédit (avec scoring automatique)

#### 3.1 - Postman / Insomnia

**Requête :**
```
POST http://localhost:8089/forsaPidev/api/credits
Content-Type: application/json

Body:
{
  "amountRequested": 15000,
  "durationMonths": 24,
  "typeCalcul": "AMORTISSEMENT_CONSTANT"
}
```

**Réponse attendue (200 OK) :**
```json
{
  "id": 1,
  "amountRequested": 15000,
  "interestRate": 5.5,
  "durationMonths": 24,
  "status": "UNDER_REVIEW",
  "requestDate": "2026-02-28T...",
  "typeCalcul": "AMORTISSEMENT_CONSTANT",
  "riskScore": 0.23,
  "isRisky": false,
  "riskLevel": "LOW",
  "scoredAt": "2026-02-28T..."
}
```

**Points clés à vérifier :**
- ✅ `status` = `UNDER_REVIEW` (et non `SUBMITTED`)
- ✅ `riskScore` est présent et a une valeur (ex: 0.23)
- ✅ `riskLevel` = `LOW`, `MEDIUM` ou `HIGH`
- ✅ `scoredAt` est rempli avec un timestamp

#### 3.2 - Vérifier les logs

**Logs Spring Boot :**
```
INFO  - Création d'une nouvelle demande de crédit pour un montant de 15000
INFO  - Lancement du scoring IA pour la demande de crédit ID=1
INFO  - Appel du service IA de scoring sur http://localhost:8000/predict
INFO  - Score IA reçu avec succès : score=0.23, risky=false
INFO  - Scoring terminé pour crédit ID=1 : score=0.23, risky=false, level=LOW
INFO  - Demande de crédit créée avec succès - ID=1, Score=0.23, Risque=LOW
```

**Logs Service IA (Python) :**
```
INFO: 127.0.0.1:xxxxx - "POST /predict HTTP/1.1" 200 OK
```

Si vous voyez ces logs → ✅ **L'intégration fonctionne !**

---

### Étape 4 : Tester la consultation des crédits en attente (endpoint agent)

**Requête :**
```
GET http://localhost:8089/forsaPidev/api/credits/pending
```

**Réponse attendue :**
```json
[
  {
    "id": 1,
    "amountRequested": 15000,
    "status": "UNDER_REVIEW",
    "riskScore": 0.23,
    "riskLevel": "LOW",
    "isRisky": false
  }
]
```

✅ On voit bien le crédit avec son score.

---

### Étape 5 : Tester l'approbation par l'agent

**Requête :**
```
POST http://localhost:8089/forsaPidev/api/credits/1/approve
```

**Réponse attendue (200 OK) :**
```json
{
  "id": 1,
  "status": "APPROVED",
  "riskScore": 0.23,
  "riskLevel": "LOW",
  ...
}
```

**Logs attendus :**
```
INFO  - Approbation de la demande de crédit ID=1
INFO  - Crédit ID=1 approuvé par l'agent - Score IA était : 0.23, Risque : LOW
```

✅ Le statut est passé à `APPROVED`, le score est toujours présent.

---

### Étape 6 : Tester le rejet par l'agent

Créer une nouvelle demande :
```
POST http://localhost:8089/forsaPidev/api/credits
{
  "amountRequested": 50000,
  "durationMonths": 60
}
```

Puis la rejeter :
```
POST http://localhost:8089/forsaPidev/api/credits/2/reject
Content-Type: application/json

{
  "reason": "Montant trop élevé et durée excessive"
}
```

**Réponse attendue :**
```json
{
  "id": 2,
  "status": "DEFAULTED",
  "riskScore": 0.xx,
  ...
}
```

**Logs :**
```
INFO  - Rejet de la demande de crédit ID=2
INFO  - Crédit ID=2 rejeté par l'agent - Score IA était : 0.xx, Raison : Montant trop élevé...
```

---

### Étape 7 : Tester le cas d'erreur (service IA indisponible)

#### 7.1 - Arrêter le service IA

Fermer le terminal où tourne le serveur Python (Ctrl+C).

#### 7.2 - Créer une nouvelle demande

```
POST http://localhost:8089/forsaPidev/api/credits
{
  "amountRequested": 10000,
  "durationMonths": 12
}
```

**Réponse attendue (200 OK, mais statut SUBMITTED) :**
```json
{
  "id": 3,
  "status": "SUBMITTED",
  "riskScore": null,
  "riskLevel": null,
  "scoredAt": null
}
```

**Logs Spring Boot :**
```
INFO  - Lancement du scoring IA pour la demande de crédit ID=3
ERROR - Impossible de joindre le service IA sur http://localhost:8000/predict : Connection refused
WARN  - Le scoring IA a échoué pour la demande ID=3 - Demande laissée en statut SUBMITTED pour revue manuelle
```

✅ **Comportement correct** : la demande est créée mais sans score, l'agent devra décider manuellement.

#### 7.3 - Redémarrer le service IA

Relancer le serveur Python, puis retester la création d'une demande → le scoring devrait à nouveau fonctionner.

---

## 🐛 Dépannage

### Erreur : Connection refused

**Cause :** Le service IA n'est pas démarré ou tourne sur un autre port.

**Solution :**
- Vérifier que le service IA est bien démarré
- Vérifier le port (doit être 8000)
- Vérifier l'URL dans `application.properties` : `ai.scoring.base-url=http://localhost:8000`

### Erreur : 400 Bad Request de l'IA

**Cause :** Les features envoyées ne correspondent pas au format attendu par le modèle.

**Solution :**
- Vérifier les logs de l'IA pour voir l'erreur exacte
- Vérifier que tous les champs dans `ScoringRequestDto` correspondent au contrat de l'IA
- Vérifier les types (int vs double, etc.)

### Erreur : riskScore reste null

**Cause :** Le scoring n'a pas été appelé ou a échoué silencieusement.

**Solution :**
- Vérifier les logs Spring pour voir si `CreditScoringService.scoreCredit()` est bien appelé
- Vérifier qu'il n'y a pas d'exception catchée
- Vérifier que `ai.scoring.enabled=true` dans la config

### Erreur : 500 Internal Server Error

**Cause :** Exception non gérée côté Spring.

**Solution :**
- Consulter la stack trace complète dans les logs Spring
- Vérifier que toutes les dépendances sont injectées correctement
- Vérifier que la base de données est accessible

---

## ✅ Checklist de validation complète

- [ ] Service IA démarre et répond sur `http://localhost:8000/predict`
- [ ] Spring Boot démarre sans erreur
- [ ] Création d'une demande de crédit → statut `UNDER_REVIEW` + score présent
- [ ] Les logs montrent l'appel IA et le score reçu
- [ ] Endpoint `/api/credits/pending` retourne les crédits avec score
- [ ] Approbation d'un crédit fonctionne (statut → `APPROVED`)
- [ ] Rejet d'un crédit fonctionne (statut → `DEFAULTED`)
- [ ] En cas de service IA down, la demande reste en `SUBMITTED` sans bloquer

---

## 🎉 Si tous les tests passent

**Félicitations !** L'intégration entre Spring Boot et le service IA de scoring est opérationnelle.

Prochaines étapes :
1. Implémenter le calcul des vraies features dans `CreditScoringService.buildFeatures()`
2. Connecter aux entités `User`, `Wallet`, historique de transactions
3. Tester avec des données réelles variées
4. Préparer le déploiement Docker (orchestration des deux services)

---

## 📚 Ressources

- Documentation API : `ARCHITECTURE_SCORING_IA.md`
- Logs Spring : console IntelliJ ou `target/logs/`
- Logs IA : console du serveur Python
- Configuration : `src/main/resources/application.properties`

