# Architecture du Scoring IA pour Forsa

## 📋 Vue d'ensemble

Le système de scoring IA analyse automatiquement chaque demande de crédit pour évaluer le risque client et aider les agents dans leur décision d'approbation.

---

## 🏗️ Architecture

### Composants

1. **Service IA Externe** (Python)
   - URL : `http://localhost:8000/predict` (local) ou `http://scoring-ia:8000/predict` (Docker)
   - Méthode : POST
   - Entrée : Features client/crédit (JSON)
   - Sortie : Score de risque (0.0 à 1.0) + indicateur `risky`

2. **Client IA** (`ScoringIaClient`)
   - Couche technique d'appel HTTP
   - Gestion des timeouts et erreurs réseau
   - Sérialisation/désérialisation JSON

3. **Service Métier de Scoring** (`CreditScoringService`)
   - Construction des features à partir des données métier
   - Interprétation du score IA
   - Détermination du niveau de risque (LOW/MEDIUM/HIGH)

4. **Service Crédit** (`CreditRequestService`)
   - Orchestration : création de demande + scoring automatique
   - Méthodes pour approbation/rejet par l'agent

5. **Contrôleur REST** (`CreditRequestController`)
   - Endpoints pour créer une demande
   - Endpoints agent : liste des crédits en revue, approbation, rejet

---

## 🔄 Workflow Complet

### 1. Client crée une demande de crédit

```
POST /api/credits
Body: {
  "amountRequested": 10000,
  "durationMonths": 24,
  "typeCalcul": "AMORTISSEMENT_CONSTANT"
}
```

**Actions du système :**
1. Création de la `CreditRequest` (statut initial : `SUBMITTED`)
2. Calcul du taux d'intérêt via le moteur TMM
3. **Appel automatique au service IA de scoring** :
   - Construction des features depuis les données client/crédit
   - Envoi à `POST http://localhost:8000/predict`
   - Réception du score et niveau de risque
4. Mise à jour de la demande :
   - `riskScore` : score retourné (ex: 0.23)
   - `isRisky` : booléen (true si score >= seuil)
   - `riskLevel` : LOW/MEDIUM/HIGH
   - `status` : passage à `UNDER_REVIEW`
   - `scoredAt` : timestamp du scoring

**Réponse au client :**
```json
{
  "id": 123,
  "amountRequested": 10000,
  "durationMonths": 24,
  "status": "UNDER_REVIEW",
  "riskScore": 0.23,
  "riskLevel": "LOW",
  "scoredAt": "2026-02-28T10:30:00"
}
```

### 2. Agent consulte les demandes en attente

```
GET /api/credits/pending
```

**Réponse :**
```json
[
  {
    "id": 123,
    "amountRequested": 10000,
    "status": "UNDER_REVIEW",
    "riskScore": 0.23,
    "riskLevel": "LOW",
    "isRisky": false
  },
  {
    "id": 124,
    "amountRequested": 50000,
    "status": "UNDER_REVIEW",
    "riskScore": 0.85,
    "riskLevel": "HIGH",
    "isRisky": true
  }
]
```

L'agent voit pour chaque demande :
- Les informations du crédit
- Le **score IA** et le **niveau de risque**
- Il peut alors prendre sa décision en connaissance de cause

### 3. Agent approuve ou rejette

#### Approbation
```
POST /api/credits/123/approve
```

**Actions :**
- Statut passe à `APPROVED`
- Le crédit peut ensuite passer par le workflow assurance → activation → génération échéancier

#### Rejet
```
POST /api/credits/124/reject
Body: { "reason": "Risque trop élevé" }
```

**Actions :**
- Statut passe à `DEFAULTED` (ou un futur statut `REJECTED`)
- Le crédit est fermé

---

## 📊 Contrat API IA

### Entrée (Features)

Le service Spring envoie un JSON avec **13 features** :

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

**Mapping métier → features :**

| Feature | Source de données | Statut |
|---------|-------------------|--------|
| `avg_delay_days` | Moyenne des jours de retard sur historique de paiements | ⚠️ TODO : à calculer |
| `payment_instability` | Écart-type des retards de paiement | ⚠️ TODO : à calculer |
| `credit_utilization` | Montant utilisé / limite autorisée | ⚠️ TODO : à calculer |
| `monthly_transaction_count` | Nombre de transactions mensuelles (wallet) | ⚠️ TODO : à calculer |
| `transaction_amount_std` | Écart-type des montants de transactions | ⚠️ TODO : à calculer |
| `high_risk_country_transaction` | Transactions dans pays à risque (0/1) | ⚠️ TODO : à calculer |
| `unusual_night_transaction` | Transactions nocturnes inhabituelles (0/1) | ⚠️ TODO : à calculer |
| `address_changed` | Changement d'adresse récent (0/1) | ⚠️ TODO : à calculer |
| `phone_changed` | Changement de téléphone récent (0/1) | ⚠️ TODO : à calculer |
| `email_changed` | Changement d'email récent (0/1) | ⚠️ TODO : à calculer |
| `country_changed` | Changement de pays récent (0/1) | ⚠️ TODO : à calculer |
| `income_change_percentage` | % variation du revenu déclaré | ⚠️ TODO : à calculer |
| `employment_changed` | Changement d'emploi récent (0/1) | ⚠️ TODO : à calculer |

> **Note actuelle :** Pour l'instant, `CreditScoringService.buildFeatures()` utilise des valeurs par défaut/neutres.  
> Il faudra enrichir cette méthode pour calculer les vraies features à partir des entités `User`, `Wallet`, historique de transactions, etc.

### Sortie (Score)

Le service IA renvoie **tout le résultat complet** :

```json
{
  "score": 0.23,
  "risky": false,
  "risk_level": "LOW"
}
```

- `score` : probabilité de risque (0.0 = faible risque, 1.0 = risque max)
- `risky` : booléen déterminé par l'IA
- `risk_level` : niveau de risque déterminé par l'IA (LOW/MEDIUM/HIGH)

**Spring utilise directement ces valeurs sans les recalculer.**

---

## ⚙️ Configuration

Dans `application.properties` :

```properties
# AI Scoring Service Configuration
ai.scoring.enabled=true
ai.scoring.base-url=http://localhost:8000
ai.scoring.predict-path=/predict
ai.scoring.connect-timeout-ms=5000
ai.scoring.read-timeout-ms=10000
ai.scoring.risk-threshold=0.7
```

**Paramètres :**
- `enabled` : activer/désactiver le scoring IA
- `base-url` : URL du service IA (localhost en dev, hostname Docker en prod)
- `predict-path` : chemin de l'endpoint de prédiction
- `connect-timeout-ms` : timeout de connexion
- `read-timeout-ms` : timeout de lecture
- `risk-threshold` : seuil au-delà duquel un client est considéré risqué (0.7 = 70%)

---

## 🛡️ Gestion des erreurs

### Service IA indisponible

Si le service IA ne répond pas (down, timeout, erreur réseau) :

1. `ScoringIaClient` lève une `ScoringServiceException`
2. `CreditRequestService` attrape l'exception
3. La demande reste en statut `SUBMITTED` (pas `UNDER_REVIEW`)
4. L'agent voit la demande mais sans score IA
5. L'agent peut décider manuellement

**Logs :**
```
WARN - Le scoring IA a échoué pour la demande ID=123 - Demande laissée en statut SUBMITTED pour revue manuelle
```

### Données invalides

Si les features envoyées sont invalides (erreur 400) :
- Exception levée
- Demande reste en `SUBMITTED`
- Log d'erreur avec détails

### Erreur interne IA (500)

- Exception levée
- Demande reste en `SUBMITTED`
- Log d'erreur

---

## 🔐 Sécurité & Conformité

### Données sensibles

Les features envoyées à l'IA ne contiennent **pas** :
- Noms, prénoms
- Numéros de compte
- Mots de passe
- Données bancaires détaillées

Uniquement des **indicateurs agrégés** (moyennes, compteurs, flags).

### Logs

Les logs contiennent :
- ID du crédit
- Score reçu
- Niveau de risque
- Timestamp

Les features complètes ne sont **pas** loguées par défaut (éviter la surcharge et la fuite de données).

---

## 🐳 Déploiement Docker (futur)

### Structure docker-compose.yml

```yaml
services:
  forsa-app:
    # Spring Boot
    environment:
      - AI_SCORING_BASE_URL=http://scoring-ia:8000
  
  scoring-ia:
    # Service IA Python
    ports:
      - "8000:8000"
    healthcheck:
      test: ["CMD", "curl", "-f", "http://localhost:8000/health"]
```

Le hostname change de `localhost:8000` à `scoring-ia:8000` grâce aux variables d'environnement.

---

## 📈 Évolutions futures

### Court terme

1. **Calculer les vraies features** :
   - Connecter aux entités `User`, `Wallet`, transactions
   - Implémenter les calculs d'historique de paiements
   - Détecter les changements de profil (adresse, email, etc.)

2. **Endpoint de simulation** :
   - Permettre à l'agent de recalculer un score à la demande
   - `POST /api/credits/{id}/score` pour rejouer le scoring

3. **Historique de scoring** :
   - Table dédiée pour tracer tous les scores calculés (audit)
   - Version du modèle IA utilisé

### Moyen terme

1. **Tests d'intégration** :
   - Mock du service IA pour tests automatisés
   - Tests bout-en-bout avec un vrai service IA

2. **Dashboard agent** :
   - Statistiques sur les scores (distribution, taux de risque)
   - Filtre par niveau de risque

3. **Feedback loop** :
   - Enregistrer les décisions agent vs recommandations IA
   - Améliorer le modèle avec les données réelles

---

## 📝 Changelog

| Date | Version | Changements |
|------|---------|-------------|
| 2026-02-28 | 1.0 | Première implémentation du scoring IA |

---

## 👥 Contact & Support

Pour toute question sur le scoring IA :
- Consulter les logs Spring (`CreditScoringService`, `ScoringIaClient`)
- Vérifier la disponibilité du service IA sur `http://localhost:8000/predict`
- Consulter la documentation du modèle IA (projet séparé)




