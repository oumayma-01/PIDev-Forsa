# 📋 Plan d'Action - Amélioration Progressive des Features

## 🎯 Objectif

Passer des features simulées aux features calculées à partir de vraies données, **étape par étape**.

---

## 🚦 État Actuel (Résumé rapide)

| Feature | État | Données nécessaires |
|---------|------|---------------------|
| ✅ `avg_delay_days` | Partiellement fonctionnel | ✅ RepaymentSchedule existe |
| ✅ `payment_instability` | Calculé depuis avg_delay | ✅ Basé sur ci-dessus |
| ✅ `credit_utilization` | Fonctionnel | ⚠️ Limite crédit simulée |
| ⚠️ `monthly_transaction_count` | Simulé | ❌ Pas de user_id dans Transaction |
| ⚠️ `transaction_amount_std` | Simulé | ❌ Pas de user_id dans Transaction |
| ❌ `high_risk_country_transaction` | Simulé | ❌ Pas de champ country |
| ❌ `unusual_night_transaction` | Simulé | ❌ Pas de user_id dans Transaction |
| ❌ `address_changed` | Simulé | ❌ Pas d'historique profil |
| ❌ `phone_changed` | Simulé | ❌ Pas d'historique profil |
| ❌ `email_changed` | Simulé | ❌ Pas d'historique profil |
| ❌ `country_changed` | Simulé | ❌ Pas d'historique profil |
| ❌ `income_change_percentage` | Simulé | ❌ Pas de previous_income |
| ❌ `employment_changed` | Simulé | ❌ Pas de champ employment |

---

## 📝 ÉTAPE 1 : Améliorer les paiements (PRIORITÉ HAUTE)

### Objectif
Calculer les **vrais** retards de paiement au lieu de les simuler.

### Actions

#### 1.1 - Modifier l'entité `RepaymentSchedule`

Ajouter un champ pour stocker la date réelle de paiement :

```java
// Dans RepaymentSchedule.java
@Column(name = "paid_date")
private LocalDate paidDate;

public LocalDate getPaidDate() { return paidDate; }
public void setPaidDate(LocalDate paidDate) { this.paidDate = paidDate; }
```

#### 1.2 - Mettre à jour `calculateAverageDelayDays()`

```java
// Dans FeatureCalculationService.java
private double calculateAverageDelayDays(Long userId) {
    // Récupérer tous les paiements effectués pour ce user
    List<RepaymentSchedule> paidSchedules = 
        repaymentScheduleRepository.findPaidByUserId(userId);
    
    if (paidSchedules.isEmpty()) {
        return 0.0;
    }
    
    long totalDelayDays = 0;
    
    for (RepaymentSchedule schedule : paidSchedules) {
        if (schedule.getPaidDate() != null) {
            // Calcul réel : différence entre date due et date payée
            long delay = ChronoUnit.DAYS.between(
                schedule.getDueDate(), 
                schedule.getPaidDate()
            );
            if (delay > 0) { // Seulement les retards (pas les paiements en avance)
                totalDelayDays += delay;
            }
        }
    }
    
    return (double) totalDelayDays / paidSchedules.size();
}
```

#### 1.3 - Créer la requête dans repository

```java
// Dans RepaymentScheduleRepository.java
@Query("SELECT r FROM RepaymentSchedule r " +
       "WHERE r.creditRequest.user.id = :userId " +
       "AND r.status = 'PAID'")
List<RepaymentSchedule> findPaidByUserId(@Param("userId") Long userId);
```

**Impact :** ✅ `avg_delay_days` et `payment_instability` seront 100% réels.

---

## 📝 ÉTAPE 2 : Ajouter les Transactions (PRIORITÉ HAUTE)

### Objectif
Calculer les statistiques de transactions réelles.

### Actions

#### 2.1 - Modifier l'entité `Transaction`

Ajouter la relation avec `User` et les champs manquants :

```java
// Dans Transaction.java
@ManyToOne(fetch = FetchType.LAZY)
@JoinColumn(name = "user_id")
private User user;

private String country;    // Pour détecter transactions à risque
private String ipAddress;  // Optionnel : pour analyse de sécurité

// Getters/Setters
public User getUser() { return user; }
public void setUser(User user) { this.user = user; }
public String getCountry() { return country; }
public void setCountry(String country) { this.country = country; }
```

#### 2.2 - Créer `TransactionRepository`

```java
// Nouveau fichier : TransactionRepository.java
package org.example.forsapidev.Repositories;

import org.example.forsapidev.entities.WalletManagement.Transaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.Date;
import java.util.List;

public interface TransactionRepository extends JpaRepository<Transaction, Long> {
    
    // Transactions des 30 derniers jours
    @Query("SELECT t FROM Transaction t " +
           "WHERE t.user.id = :userId " +
           "AND t.timestamp >= :startDate")
    List<Transaction> findRecentTransactions(
        @Param("userId") Long userId,
        @Param("startDate") Date startDate
    );
    
    // Compte des transactions nocturnes (22h-6h)
    @Query("SELECT COUNT(t) FROM Transaction t " +
           "WHERE t.user.id = :userId " +
           "AND (HOUR(t.timestamp) >= 22 OR HOUR(t.timestamp) <= 6)")
    int countNightTransactions(@Param("userId") Long userId);
    
    // Transactions dans pays à risque
    @Query("SELECT COUNT(t) FROM Transaction t " +
           "WHERE t.user.id = :userId " +
           "AND t.country IN :riskCountries")
    int countHighRiskCountryTransactions(
        @Param("userId") Long userId,
        @Param("riskCountries") List<String> riskCountries
    );
}
```

#### 2.3 - Mettre à jour `FeatureCalculationService`

Injecter `TransactionRepository` et implémenter les vraies méthodes :

```java
// Ajouter dans le constructeur
private final TransactionRepository transactionRepository;

// Implémenter
private int calculateMonthlyTransactionCount(Long userId) {
    Date oneMonthAgo = new Date(System.currentTimeMillis() - 30L * 24 * 60 * 60 * 1000);
    List<Transaction> recent = transactionRepository.findRecentTransactions(userId, oneMonthAgo);
    return recent.size();
}

private double calculateTransactionAmountStd(Long userId) {
    Date oneMonthAgo = new Date(System.currentTimeMillis() - 30L * 24 * 60 * 60 * 1000);
    List<Transaction> recent = transactionRepository.findRecentTransactions(userId, oneMonthAgo);
    
    if (recent.size() < 2) {
        return 0.0;
    }
    
    // Calculer moyenne
    double sum = 0.0;
    for (Transaction t : recent) {
        sum += t.getAmount().doubleValue();
    }
    double mean = sum / recent.size();
    
    // Calculer écart-type
    double variance = 0.0;
    for (Transaction t : recent) {
        double diff = t.getAmount().doubleValue() - mean;
        variance += diff * diff;
    }
    variance /= recent.size();
    
    return Math.sqrt(variance);
}

private int detectUnusualNightTransactions(Long userId) {
    return transactionRepository.countNightTransactions(userId);
}

private int detectHighRiskCountryTransactions(Long userId) {
    List<String> riskCountries = Arrays.asList("XX", "YY", "ZZ"); // À adapter
    return transactionRepository.countHighRiskCountryTransactions(userId, riskCountries);
}
```

**Impact :** ✅ 4 features de transactions seront réelles.

---

## 📝 ÉTAPE 3 : Historique du Profil (PRIORITÉ MOYENNE)

### Objectif
Détecter les changements de profil client.

### Option A : Table d'historique dédiée (RECOMMANDÉ)

#### 3.1 - Créer l'entité `ProfileHistory`

```java
// Nouveau fichier : ProfileHistory.java
package org.example.forsapidev.entities.UserManagement;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "profile_history")
public class ProfileHistory {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;
    
    private String fieldName;  // 'address', 'phone', 'email', 'country'
    
    @Column(columnDefinition = "TEXT")
    private String oldValue;
    
    @Column(columnDefinition = "TEXT")
    private String newValue;
    
    private LocalDateTime changedAt;
    
    // Getters/Setters
}
```

#### 3.2 - Créer `ProfileHistoryRepository`

```java
public interface ProfileHistoryRepository extends JpaRepository<ProfileHistory, Long> {
    
    @Query("SELECT COUNT(h) FROM ProfileHistory h " +
           "WHERE h.user.id = :userId " +
           "AND h.fieldName = :fieldName " +
           "AND h.changedAt >= :since")
    int countRecentChanges(
        @Param("userId") Long userId,
        @Param("fieldName") String fieldName,
        @Param("since") LocalDateTime since
    );
}
```

#### 3.3 - Implémenter les détections

```java
private int detectAddressChange(Long userId) {
    LocalDateTime sixMonthsAgo = LocalDateTime.now().minusMonths(6);
    int changes = profileHistoryRepository.countRecentChanges(userId, "address", sixMonthsAgo);
    return changes > 0 ? 1 : 0;
}

// Idem pour phone, email, country
```

### Option B : Champs de tracking dans Profile (PLUS SIMPLE)

Modifier `Profile` :

```java
// Dans Profile.java
private String previousPhoneNumber;
private LocalDateTime phoneChangedAt;

private String previousAddress;
private LocalDateTime addressChangedAt;

// Etc.
```

**Impact :** ✅ 4 features de changements de profil seront réelles.

---

## 📝 ÉTAPE 4 : Revenus et Emploi (PRIORITÉ BASSE)

### Actions

#### 4.1 - Modifier `Profile`

```java
// Dans Profile.java
private BigDecimal previousIncome;
private LocalDateTime incomeLastUpdated;

private String employmentStatus;  // 'EMPLOYED', 'UNEMPLOYED', 'SELF_EMPLOYED'
private String previousEmploymentStatus;
private LocalDateTime employmentChangedAt;
```

#### 4.2 - Calculer les changements

```java
private double calculateIncomeChangePercentage(Long userId) {
    // TODO: Récupérer le profil
    Profile profile = profileRepository.findByUserId(userId);
    
    if (profile == null || profile.getPreviousIncome() == null) {
        return 0.0;
    }
    
    BigDecimal current = profile.getIncomeIndicator();
    BigDecimal previous = profile.getPreviousIncome();
    
    if (previous.compareTo(BigDecimal.ZERO) == 0) {
        return 0.0;
    }
    
    BigDecimal change = current.subtract(previous);
    BigDecimal percentage = change.divide(previous, 4, RoundingMode.HALF_UP)
                                  .multiply(BigDecimal.valueOf(100));
    
    return percentage.doubleValue();
}
```

**Impact :** ✅ 2 features de revenus/emploi seront réelles.

---

## 🎯 RÉCAPITULATIF PAR PRIORITÉ

### ✅ PRIORITÉ 1 (À faire EN PREMIER)

1. **Ajouter `paidDate` dans `RepaymentSchedule`**
   - Impact : 2 features (avg_delay_days, payment_instability)
   - Difficulté : ⭐ Facile
   - Temps : 30 minutes

2. **Ajouter `userId` dans `Transaction`**
   - Impact : 4 features (transactions)
   - Difficulté : ⭐⭐ Moyen
   - Temps : 1-2 heures

### ⚙️ PRIORITÉ 2 (Ensuite)

3. **Créer table `ProfileHistory` ou ajouter champs de suivi**
   - Impact : 4 features (changements profil)
   - Difficulté : ⭐⭐ Moyen
   - Temps : 2-3 heures

### 📊 PRIORITÉ 3 (Si nécessaire)

4. **Ajouter historique revenus/emploi dans `Profile`**
   - Impact : 2 features
   - Difficulté : ⭐ Facile
   - Temps : 1 heure

---

## 🧪 TESTER PROGRESSIVEMENT

Après chaque étape, testez :

1. Vérifier que la compilation fonctionne
2. Lancer Spring Boot
3. Créer une demande de crédit
4. Vérifier les logs : les nouvelles features doivent apparaître
5. Vérifier le JSON envoyé à l'IA

---

**🎉 Avec ce plan, vous pouvez améliorer vos features progressivement, feature par feature ! 🎉**

