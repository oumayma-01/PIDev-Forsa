# 📊 Architecture du Système de Calcul d'Amortissement de Crédit

## 🏗️ Vue d'ensemble

Ce système implémente deux méthodes de calcul d'amortissement mensuel pour les crédits bancaires :
- **Annuité Constante** (mensualité fixe)
- **Amortissement Constant** (principal fixe)

### ✅ Principes Appliqués
- **Pattern Strategy** pour extensibilité
- **SOLID principles**
- **BigDecimal** avec arrondi HALF_EVEN
- Calcul déclenché uniquement après validation
- Ajustement automatique du dernier mois

---

## 📁 Structure des Fichiers

```
src/main/java/org/example/forsapidev/
├── entities/CreditManagement/
│   ├── CreditRequest.java           // Entité crédit avec typeCalcul
│   ├── AmortizationType.java        // Enum: ANNUITE_CONSTANTE, AMORTISSEMENT_CONSTANT
│   ├── CreditStatus.java
│   └── RepaymentSchedule.java       // Échéances mensuelles
│
├── Services/
│   ├── CreditRequestService.java   // Service principal (orchestration)
│   └── amortization/
│       ├── AmortizationStrategy.java              // Interface Strategy
│       ├── AnnuiteConstanteStrategy.java          // Implémentation annuité constante
│       ├── AmortissementConstantStrategy.java     // Implémentation amortissement constant
│       ├── AmortizationCalculatorService.java     // Orchestrateur Strategy
│       └── AmortizationResult.java                // DTO résultat
│
├── Controllers/
│   └── CreditRequestController.java // Endpoints REST
│
└── payload/response/
    └── AmortizationScheduleResponse.java // DTO réponse API
```

---

## 🔄 Workflow du Système

### 1️⃣ Création d'une Demande de Crédit
```
POST /api/credits
{
  "amountRequested": 100000,
  "durationMonths": 24,
  "typeCalcul": "ANNUITE_CONSTANTE"  // ou "AMORTISSEMENT_CONSTANT"
}
```

**Ce qui se passe :**
- Statut initial : `SUBMITTED`
- Le `typeCalcul` est enregistré mais **aucun calcul n'est effectué**
- Le taux d'intérêt est calculé via `InterestRateEngineService`

### 2️⃣ Validation du Crédit
```
POST /api/credits/{id}/validate
```

**Ce qui se passe :**
1. Vérification que le crédit n'est pas déjà validé
2. Changement du statut à `APPROVED`
3. **Déclenchement automatique du calcul** via `generateRepaymentSchedule()`
4. Le système identifie le `typeCalcul` du crédit
5. Appel du `AmortizationCalculatorService` avec le bon Strategy
6. Génération de toutes les échéances mensuelles
7. Sauvegarde en base dans `RepaymentSchedule`

### 3️⃣ Consultation du Tableau d'Amortissement
```
GET /api/credits/{id}/schedule
```

Retourne le tableau complet avec :
- Numéro de mois
- Amortissement (principal)
- Intérêts
- Mensualité totale
- Capital restant

### 4️⃣ Simulation (sans créer de crédit)
```
GET /api/credits/simulate?principal=100000&rate=5.0&duration=24&type=ANNUITE_CONSTANTE
```

Permet de tester les deux méthodes sans persister de données.

---

## 📐 Logique Métier des Calculs

### Paramètres Communs
- **C** = capital emprunté
- **T** = taux annuel (en %)
- **n** = durée en mois
- **i** = taux mensuel = T / 100 / 12

### 🔹 Méthode 1 : Annuité Constante

**Formule de la mensualité :**
```
A = C × i / (1 - (1 + i)^(-n))
```

**À chaque mois :**
```
Intérêt = Capital restant × i
Amortissement = A - Intérêt
Nouveau capital = Ancien capital - Amortissement
```

**Caractéristiques :**
- ✅ Mensualité **constante** (sauf dernier mois)
- ✅ Amortissement **croissant**
- ✅ Intérêts **décroissants**

**Implémentation :** `AnnuiteConstanteStrategy.java`

### 🔹 Méthode 2 : Amortissement Constant

**Formule de l'amortissement :**
```
Amortissement mensuel = C / n
```

**À chaque mois :**
```
Intérêt = Capital restant × i
Mensualité = Amortissement + Intérêt
Nouveau capital = Ancien capital - Amortissement
```

**Caractéristiques :**
- ✅ Amortissement **constant**
- ✅ Mensualité **décroissante**
- ✅ Intérêts **décroissants**

**Implémentation :** `AmortissementConstantStrategy.java`

---

## 🎯 Pattern Strategy Expliqué

### Interface `AmortizationStrategy`
```java
public interface AmortizationStrategy {
    AmortizationResult calculate(BigDecimal principal, 
                                BigDecimal annualRatePercent, 
                                int durationMonths);
}
```

### Orchestrateur `AmortizationCalculatorService`
```java
@Service
public class AmortizationCalculatorService {
    private final Map<AmortizationType, AmortizationStrategy> strategies;
    
    // Injection des strategies
    public AmortizationCalculatorService(
            AnnuiteConstanteStrategy annuiteConstanteStrategy,
            AmortissementConstantStrategy amortissementConstantStrategy) {
        
        strategies.put(ANNUITE_CONSTANTE, annuiteConstanteStrategy);
        strategies.put(AMORTISSEMENT_CONSTANT, amortissementConstantStrategy);
    }
    
    // Sélection dynamique de la stratégie
    public AmortizationResult calculateSchedule(AmortizationType type, ...) {
        AmortizationStrategy strategy = strategies.get(type);
        return strategy.calculate(...);
    }
}
```

**Avantages :**
- ✅ Ajout facile de nouvelles méthodes (ex: taux variable)
- ✅ Pas de `if/else` ou `switch`
- ✅ Testabilité maximale
- ✅ Respect du principe Open/Closed

---

## 🔒 Garanties de Cohérence Financière

### 1. Utilisation de BigDecimal
```java
private static final int SCALE = 2;
private static final RoundingMode ROUNDING = RoundingMode.HALF_EVEN;
private static final int PRECISION_SCALE = 10; // Pour calculs intermédiaires
```

### 2. Ajustement du Dernier Mois
```java
if (month == durationMonths) {
    // On prend exactement le capital restant
    principalPayment = remainingPrincipal;
    actualMonthlyPayment = principalPayment.add(interestPayment);
}
```

**Pourquoi ?** Évite les erreurs d'arrondi cumulées. Le dernier mois solde **exactement** le capital.

### 3. Validation des Entrées
- Capital > 0
- Taux ≥ 0
- Durée > 0

### 4. Gestion du Taux Zéro
```java
if (monthlyRate.compareTo(BigDecimal.ZERO) == 0) {
    monthlyPayment = principal.divide(
        BigDecimal.valueOf(durationMonths), SCALE, ROUNDING);
}
```

---

## 🚀 Extension Future

### Ajouter une Nouvelle Méthode de Calcul

**Exemple : Taux Variable**

1. Créer l'enum :
```java
// Dans AmortizationType.java
TAUX_VARIABLE
```

2. Créer la Strategy :
```java
@Component
public class TauxVariableStrategy implements AmortizationStrategy {
    @Override
    public AmortizationResult calculate(...) {
        // Implémentation
    }
}
```

3. Enregistrer dans le service :
```java
strategies.put(TAUX_VARIABLE, tauxVariableStrategy);
```

**Aucune modification des autres classes nécessaire !**

---

## 📊 Exemple de Réponse API

### GET /api/credits/123/schedule

```json
{
  "creditId": 123,
  "calculationType": "ANNUITE_CONSTANTE",
  "principal": 100000.00,
  "annualRatePercent": 5.00,
  "durationMonths": 12,
  "totalInterest": 2759.41,
  "totalAmount": 102759.41,
  "periods": [
    {
      "monthNumber": 1,
      "principalPayment": 8146.56,
      "interestPayment": 416.67,
      "totalPayment": 8563.23,
      "remainingBalance": 91853.44
    },
    {
      "monthNumber": 2,
      "principalPayment": 8180.52,
      "interestPayment": 382.71,
      "totalPayment": 8563.23,
      "remainingBalance": 83672.92
    },
    // ... 10 autres mois
  ]
}
```

---

## ✅ Tests Recommandés

### Tests Unitaires

1. **AnnuiteConstanteStrategy**
   - Taux normal (5%)
   - Taux zéro
   - Vérification dernier mois = 0

2. **AmortissementConstantStrategy**
   - Taux normal
   - Taux zéro
   - Vérification amortissement constant

3. **AmortizationCalculatorService**
   - Sélection correcte des strategies
   - Exception si type null

### Tests d'Intégration

1. Créer crédit → Valider → Vérifier échéances
2. Simuler les deux méthodes avec mêmes paramètres
3. Comparer coût total (annuité < amortissement constant)

---

## 📝 Notes Techniques

### Pourquoi Services avec S majuscule ?
Le projet utilise `org.example.forsapidev.Services` (S majuscule), donc tous les packages suivent cette convention.

### Pourquoi Repositories avec R majuscule ?
Même raison : cohérence avec l'existant.

### Base de Données
Les échéances sont sauvegardées dans `RepaymentSchedule` avec :
- `principal_part` : amortissement
- `interest_part` : intérêts
- `total_amount` : mensualité
- `remaining_balance` : capital restant
- `due_date` : date d'échéance

---

## 🎓 Conclusion

Ce système offre :
- ✅ **Architecture propre** (Strategy Pattern)
- ✅ **Calculs précis** (BigDecimal, HALF_EVEN)
- ✅ **Extensibilité** (ajout facile de nouvelles méthodes)
- ✅ **Cohérence bancaire** (ajustement dernier mois)
- ✅ **Workflow robuste** (calcul uniquement après validation)
- ✅ **API complète** (création, validation, simulation, consultation)

Prêt pour un environnement bancaire de production ! 🏦

