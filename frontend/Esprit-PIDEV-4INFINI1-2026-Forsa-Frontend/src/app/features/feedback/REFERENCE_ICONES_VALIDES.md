# 📋 RÉFÉRENCE COMPLÈTE - ICÔNES FORSA VALIDES

**Pour les corrections manuelles des fichiers externes**

---

## ✅ Icônes Valides Disponibles

Voici la liste **COMPLÈTE** des icônes valides dans `forsa-icon.types.ts`:

```typescript
'alert-circle'        // ⚠️ Alerte, erreur
'arrow-right'         // → Navigation, répondre
'arrow-up-right'      // ↗️ Verso, nouveau
'arrow-down-right'    // ↘️ Verso
'bell'                // 🔔 Notifications
'brain'               // 🧠 Intelligence
'car'                 // 🚗 Assurance, voiture
'check-circle-2'      // ✓ Succès, résolu
'chevron-down'        // ▼ Ouvrir menu
'chevron-left'        // ◀ Navigation
'chevron-right'       // ▶ Navigation
'clock'               // 🕐 Temps, en cours
'credit-card'         // 💳 Paiement
'eye'                 // 👁️ Voir
'eye-off'             // 👁️‍🗨️ Cacher
'filter'              // 🔍 Filtrer, catégories
'heart'               // ❤️ Favoris, rating
'history'             // 📜 Historique, refresh
'home'                // 🏠 Accueil
'layout-dashboard'    // 📊 Tableau de bord
'log-out'             // 🚪 Déconnexion
'message-square'      // 💬 Messages, feedback
'moon'                // 🌙 Thème sombre
'more-horizontal'     // ⋯ Plus d'options
'more-vertical'       // ⋮ Plus d'options
'pencil'              // ✏️ Éditer
'plus'                // + Ajouter
'power'               // ⏻ Alimentation
'search'              // 🔍 Recherche
'send'                // 📤 Envoyer
'settings'            // ⚙️ Paramètres
'shield'              // 🛡️ Sécurité
'shield-alert'        // ⚠️ Alerte sécurité
'shield-check'        // ✓ Sécurité OK
'sparkles'            // ✨ Intelligence, spécial
'sun'                 // ☀️ Thème clair
'trash-2'             // 🗑️ Supprimer
'trending-down'       // 📉 Baisse
'trending-up'         // 📈 Hausse
'users'               // 👥 Utilisateurs
'user-circle'         // 👤 Profil
'wallet'              // 💰 Portefeuille
'zap'                 // ⚡ Charge, action rapide
'bar-chart-3'         // 📊 Statistiques
```

---

## ❌ Icônes INVALIDES (Ne pas utiliser)

Voici ce qu'il **NE FAUT PAS** utiliser:

| Invalide | À Utiliser | Raison |
|----------|-----------|--------|
| `loader` | `zap` ou `sparkles` | N'existe pas, utiliser un équivalent |
| `check` | `check-circle-2` | Utiliser la version avec cercle |
| `star` | `heart` | Star n'existe pas |
| `message-circle` | `message-square` | Cercle n'existe pas |
| `inbox` | `message-square` | Inbox n'existe pas |
| `trash` | `trash-2` | Utiliser la version 2 |
| `edit-2` | `pencil` | Pencil est le bon nom |
| `x-circle` | `alert-circle` | X-circle n'existe pas |
| `reply` | `arrow-right` | Reply n'existe pas |
| `tag` | `filter` | Tag n'existe pas, filter est plus proche |
| `refresh-cw` | `history` | Refresh-cw n'existe pas |
| `ban` | `alert-circle` | Ban n'existe pas |
| `info` | `alert-circle` | Info n'existe pas comme icône |

---

## 🔧 Corrections à Appliquer

### 1. src/app/core/data/mock-data.ts
```typescript
// Changez la ligne 2
- import { Complaint, ... } from '../models/forsa.models';
+ import { ComplaintBackend, ... } from '../models/forsa.models';
```

### 2. src/app/features/feedbackandcomplaintmanagement/complaint/complaint-form.component.html
```html
<!-- Ligne 94 -->
- <app-forsa-icon name="loader" [size]="16" />
+ <app-forsa-icon name="zap" [size]="16" />

<!-- Ligne 97 -->
- <app-forsa-icon name="check" [size]="16" />
+ <app-forsa-icon name="check-circle-2" [size]="16" />
```

### 3. src/app/features/feedbackandcomplaintmanagement/feedback_form/feedback-form.component.html
```html
<!-- Ligne 82 -->
- <app-forsa-icon name="loader" [size]="16" />
+ <app-forsa-icon name="zap" [size]="16" />

<!-- Ligne 85 -->
- <app-forsa-icon name="check" [size]="16" />
+ <app-forsa-icon name="check-circle-2" [size]="16" />
```

---

## 📚 Icônes par Catégorie

### Navigation (4)
- `arrow-right` - Aller au suivant, répondre
- `arrow-up-right` - Ouvrir
- `arrow-down-right` - Déplier
- `chevron-right` - Petit chevron droit

### Statuts/Actions (6)
- `check-circle-2` - ✓ Succès
- `alert-circle` - ⚠️ Erreur, alerte
- `clock` - 🕐 En cours, attente
- `power` - ⏻ Actif/Inactif

### Opérations (5)
- `plus` - Ajouter
- `pencil` - Éditer
- `trash-2` - Supprimer
- `send` - Envoyer
- `filter` - Filtrer

### Communication (2)
- `message-square` - Messages
- `bell` - Notifications

### Données (4)
- `bar-chart-3` - Graphiques
- `history` - Historique
- `trending-up` - Augmentation
- `trending-down` - Diminution

### Thème/UI (4)
- `sun` - Mode clair
- `moon` - Mode sombre
- `settings` - Paramètres
- `more-vertical` - Plus d'options

### Spéciales (5)
- `heart` - Favoris, rating
- `sparkles` - Intelligence, spécial
- `zap` - Action rapide, charge
- `brain` - Intelligence
- `wallet` - Portefeuille

---

## ✅ Checklist de Correction

- [ ] Mock-data.ts: Complaint → ComplaintBackend
- [ ] complaint-form.html ligne 94: loader → zap
- [ ] complaint-form.html ligne 97: check → check-circle-2
- [ ] feedback-form.html ligne 82: loader → zap
- [ ] feedback-form.html ligne 85: check → check-circle-2

---

## 🎯 Après les Corrections

Exécutez:
```bash
ng serve
```

Vous devriez voir:
```
✅ Compiled successfully
✅ Listening on http://localhost:4200/
```

---

**Référence créée:** 2026-04-18  
**Icônes disponibles:** 42  
**Icônes corrigies:** 13
