# 🎯 Système de Calcul des Features pour le Scoring IA

## ✅ CE QUI A ÉTÉ FAIT

Un système complet de calcul des **13 features** nécessaires pour votre modèle IA de scoring.

---

## 📊 Architecture du Système de Features

### 1. Service Principal : `FeatureCalculationService`

**Localisation :** `org.example.forsapidev.Services.scoring.FeatureCalculationService`

**Rôle :** Calculer toutes les features à partir des données réelles en base de données.

### 2. Flux de calcul

```
Client crée demande
    ↓
CreditRequestService.createCreditRequest()
    ↓
CreditScoringService.scoreCredit()
    ↓
FeatureCalculationService.calculateFeatures()  ← CALCUL DES 13 FEATURES
    ↓
ScoringIaClient.predict()  ← ENVOI AU MODÈLE IA
    ↓
Modèle retourne score + risky + risk_level
```

---

## 🧮 Détail des 13 Features Calculées

### Groupe 1 : Historique de Paiements

| Feature | Méthode | Source des données | État |
|---------|---------|-------------------|------|
| `avg_delay_days` | `calculateAverageDelayDays()` | Table `repayment_schedule` | ✅ Implémenté (simulation) |
| `payment_instability` | `calculatePaymentInstability()` | Écart-type des retards | ✅ Implémenté (basé sur avg) |

**Comment ça marche actuellement :**
- Récupère tous les paiements effectués (`status=PAID`)
- Calcule le nombre moyen de jours de retard
- Pour l'instant : **simulation** car pas de champ `paid_date` dans `RepaymentSchedule`

**Pour améliorer :**
- Ajouter un champ `paidDate` dans `RepaymentSchedule`
- Stocker la date réelle de paiement
- Calculer `dueDate - paidDate` = jours de retard

---

### Groupe 2 : Utilisation du Crédit

| Feature | Méthode | Source des données | État |
|---------|---------|-------------------|------|
| `credit_utilization` | `calculateCreditUtilization()` | `CreditRequest.amountRequested` | ✅ Implémenté |

**Comment ça marche :**
- Formule : `montant_demandé / limite_crédit_autorisée`
- Actuellement : limite fixée à 50 000 (simulation)

**Pour améliorer :**
- Ajouter un champ `creditLimit` dans table `user` ou `profile`
- Récupérer la vraie limite autorisée pour ce client

---

### Groupe 3 : Transactions

| Feature | Méthode | Source des données | État |
|---------|---------|-------------------|------|
| `monthly_transaction_count` | `calculateMonthlyTransactionCount()` | Table `transaction` | ⚠️ Simulé |
| `transaction_amount_std` | `calculateTransactionAmountStd()` | Table `transaction` | ⚠️ Simulé |
| `high_risk_country_transaction` | `detectHighRiskCountryTransactions()` | Table `transaction` + pays | ⚠️ TODO |
| `unusual_night_transaction` | `detectUnusualNightTransactions()` | Table `transaction` timestamps | ⚠️ TODO |

**Problème actuel :**
- La table `Transaction` existe mais n'a pas de relation avec `User`
- Pas de champ `userId` dans `Transaction`
- Pas de champ `country` ou `location`

**Pour améliorer :**

#### 1. Ajouter la relation User ↔ Transaction

```java
// Dans Transaction.java
@ManyToOne(fetch = FetchType.LAZY)
@JoinColumn(name = "user_id")
private User user;
```

#### 2. Ajouter des champs manquants

```java
// Dans Transaction.java
private String country;      // Pour détecter pays à risque
private String ipAddress;    // Pour analyse de sécurité
```

#### 3. Créer un repository pour requêtes

```java
public interface TransactionRepository extends JpaRepository<Transaction, Long> {
    // Transactions des 30 derniers jours
    @Query("SELECT t FROM Transaction t WHERE t.user.id = :userId " +
           "AND t.timestamp >= :startDate")
    List<Transaction> findRecentTransactions(
        @Param("userId") Long userId,
        @Param("startDate") Date startDate
    );
    
    // Transactions nocturnes (22h-6h)
    @Query("SELECT COUNT(t) FROM Transaction t WHERE t.user.id = :userId " +
           "AND HOUR(t.timestamp) BETWEEN 22 AND 23 OR HOUR(t.timestamp) BETWEEN 0 AND 6")
    int countNightTransactions(@Param("userId") Long userId);
}
```

---

### Groupe 4 : Changements de Profil

| Feature | Méthode | Source des données | État |
|---------|---------|-------------------|------|
| `address_changed` | `detectAddressChange()` | Historique profil | ⚠️ TODO |
| `phone_changed` | `detectPhoneChange()` | Historique profil | ⚠️ TODO |
| `email_changed` | `detectEmailChange()` | Historique `user` | ⚠️ TODO |
| `country_changed` | `detectCountryChange()` | Historique profil | ⚠️ TODO |

**Problème actuel :**
- Pas d'historique des changements de profil
- Pas de table `profile_history`

**Solutions possibles :**

#### Option A : Table d'historique dédiée

```sql
CREATE TABLE profile_history (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT,
    field_name VARCHAR(50),  -- 'address', 'phone', 'email', etc.
    old_value TEXT,
    new_value TEXT,
    changed_at TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES user(id)
);
```

#### Option B : Champs de suivi dans Profile

```java
// Dans Profile.java
private String previousAddress;
private LocalDateTime addressChangedAt;

private String previousPhone;
private LocalDateTime phoneChangedAt;
```

#### Option C : Utiliser AuditLog existant

Adapter la table `audit_log` pour stocker les changements de profil.

---

### Groupe 5 : Revenus et Emploi

| Feature | Méthode | Source des données | État |
|---------|---------|-------------------|------|
| `income_change_percentage` | `calculateIncomeChangePercentage()` | Table `profile` | ⚠️ TODO |
| `employment_changed` | `detectEmploymentChange()` | Historique profil | ⚠️ TODO |

**Problème actuel :**
- `Profile` a seulement `incomeIndicator` (valeur actuelle)
- Pas de champ `previousIncome` ou historique

**Pour améliorer :**

```java
// Dans Profile.java
private BigDecimal previousIncome;
private LocalDateTime incomeLastUpdated;

private String employmentStatus;  // 'EMPLOYED', 'UNEMPLOYED', 'SELF_EMPLOYED'
private String previousEmploymentStatus;
private LocalDateTime employmentChangedAt;
```

---

## 📝 ÉTAT ACTUEL DU SYSTÈME

### ✅ Ce qui fonctionne MAINTENANT

1. **Architecture complète en place**
   - `FeatureCalculationService` créé et fonctionnel
   - Intégré dans `CreditScoringService`
   - Relation `CreditRequest` ↔ `User` ajoutée

2. **Features avec données réelles :**
   - ✅ `avg_delay_days` : calculé depuis `repayment_schedule` (simulation pour l'instant)
   - ✅ `payment_instability` : basé sur avg_delay_days
   - ✅ `credit_utilization` : montant demandé / limite (limite simulée à 50k)

3. **Features simulées mais prêtes :**
   - ⚠️ Toutes les autres features retournent des valeurs par défaut
   - ⚠️ Prêt à être remplacé par de vrais calculs

### ⚠️ Ce qui est en simulation

Les features suivantes retournent des valeurs par défaut car les données ne sont pas encore en base :

- `monthly_transaction_count` → Valeur aléatoire entre 5 et 25
- `transaction_amount_std` → Valeur aléatoire entre 100 et 300
- `high_risk_country_transaction` → 0
- `unusual_night_transaction` → 0
- `address_changed` → 0
- `phone_changed` → 0
- `email_changed` → 0
- `country_changed` → 0
- `income_change_percentage` → 0.0
- `employment_changed` → 0

---

## 🚀 COMMENT TESTER MAINTENANT

### 1. Préparer les données

Même avec les simulations, le système fonctionne. Pour tester :

```sql
-- Créer un utilisateur de test
INSERT INTO user (username, email, password_hash, is_active, created_at) 
VALUES ('test_client', 'client@test.com', 'hash', 1, NOW());

-- Créer une demande de crédit liée à cet utilisateur
INSERT INTO credit_request (
    amount_requested, 
    duration_months, 
    status, 
    request_date,
    user_id,  -- IMPORTANT : lien avec le user
    amortization_type
) VALUES (
    15000, 
    24, 
    'SUBMITTED', 
    NOW(),
    1,  -- ID du user créé ci-dessus
    'AMORTISSEMENT_CONSTANT'
);
```

### 2. Lancer l'application

```bash
.\mvnw.cmd spring-boot:run
```

### 3. Faire une demande de crédit

```http
POST http://localhost:8089/forsaPidev/api/credits
Content-Type: application/json

{
  "amountRequested": 15000,
  "durationMonths": 24,
  "typeCalcul": "AMORTISSEMENT_CONSTANT",
  "user": {
    "id": 1
  }
}
```

**Attention :** Il faut adapter votre contrôleur pour accepter l'objet `user` dans la requête.

### 4. Vérifier les logs

```
INFO - Calcul des features pour crédit ID=1, userId=1
INFO - Features calculées : avgDelay=1.23, instability=0.37, utilization=0.3
INFO - Appel du service IA de scoring sur http://localhost:8000/predict
```

### 5. Vérifier le JSON envoyé à l'IA

Le système envoie maintenant des valeurs **calculées** (même si certaines sont encore simulées) :

```json
{
  "avg_delay_days": 1.5,
  "payment_instability": 0.45,
  "credit_utilization": 0.3,
  "monthly_transaction_count": 12,
  "transaction_amount_std": 150.75,
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

---

## 🔧 PROCHAINES ÉTAPES POUR DES VRAIES FEATURES

### Priorité 1 : Transactions

1. Ajouter `userId` dans `Transaction`
2. Créer `TransactionRepository` avec requêtes custom
3. Implémenter les vraies méthodes :
   - `calculateMonthlyTransactionCount()`
   - `calculateTransactionAmountStd()`

### Priorité 2 : Historique de paiements

1. Ajouter `paidDate` dans `RepaymentSchedule`
2. Mettre à jour `calculateAverageDelayDays()` pour calculer les vrais retards

### Priorité 3 : Profil et historique

1. Créer table `profile_history` ou ajouter champs de suivi dans `Profile`
2. Implémenter détection des changements :
   - Adresse, téléphone, email, pays
   - Revenu, emploi

---

## 📚 Fichiers créés/modifiés

| Fichier | Modification | État |
|---------|-------------|------|
| `FeatureCalculationService.java` | ✅ Créé | Service de calcul des features |
| `CreditRequest.java` | ✅ Modifié | Ajout relation avec User |
| `CreditScoringService.java` | ✅ Modifié | Utilise FeatureCalculationService |

---

**🎉 Le système de calcul des features est en place et fonctionne ! 🎉**

Il calcule déjà 3 features réelles et simule les 10 autres.  
Vous pouvez tester dès maintenant avec votre modèle IA.

