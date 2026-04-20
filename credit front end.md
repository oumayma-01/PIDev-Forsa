## Gestion du rapport de risque et interface agent

### Fonctionnement
- Lorsqu’un crédit est créé, le backend génère automatiquement un rapport de risque (scoring IA, analyse fraude, etc.).
- Si le crédit n’a pas encore d’agent assigné, le rapport de risque est stocké et reste associé à la demande.
- Dès qu’un agent est assigné à ce crédit, le rapport de risque lui est automatiquement transmis (accessible dans son interface).

### Interface côté agent
- Dans l’état actuel du backend, la liste “à traiter” côté agent provient de `GET /api/credits/pending`.
  - Remarque : cet endpoint renvoie les crédits en attente, mais ne permet pas de filtrer “uniquement mes crédits assignés” car il n’existe pas d’endpoint dédié (et `agentId` correspond à l’entité `Agent`, pas forcément au `user.id`).
- Pour chaque crédit, un bouton ou un lien permet d’ouvrir le détail, où le rapport de risque est affiché (score, niveau de risque, rapport PDF, etc.).
- Si un crédit vient d’être assigné à un agent, le rapport de risque apparaît instantanément dans l’interface de cet agent.
- Les crédits sans agent restent en attente, et leur rapport de risque n’est visible que lorsqu’un agent est désigné.

### Points d’implémentation frontend
- Prévoir dans l’interface agent une section "Rapport de risque" dans le détail du crédit.
- Rafraîchir la liste des crédits à traiter pour afficher les nouveaux crédits assignés et leurs rapports.
- Afficher un indicateur si un rapport de risque est en attente d’assignation (optionnel, pour les admins).

---
# Intégration Frontend Crédit

## Interfaces et accès par rôle

### 1. CLIENT
- **Accès** :
  - Simulation de crédit
  - Création de demande de crédit
  - Consultation de ses propres crédits et statuts
  - Visualisation du tableau d’amortissement de ses crédits validés
  - Paiement de ses propres échéances
- **Interface dédiée** :
  - Tableau de bord personnel (liste de ses crédits, statut, actions possibles)
  - Formulaire de simulation et de demande
  - Page d’échéancier et bouton de paiement

### 2. AGENT
- **Accès** :
  - Liste des crédits à traiter (affectés ou en attente)
  - Validation, approbation ou rejet des crédits
  - Visualisation des détails d’un crédit (y compris scoring, rapport médical, etc.)
  - Suivi des échéances des crédits gérés
- **Interface dédiée** :
  - Tableau de bord agent (crédits à traiter, filtres par statut)
  - Pages de validation/rejet avec formulaire de décision
  - Accès à l’historique des crédits traités

### 3. ADMIN
- **Accès** :
  - Vue globale sur tous les crédits et utilisateurs
  - Statistiques et reporting (nombre de crédits, montants, taux de validation, etc.)
  - Gestion des utilisateurs et des rôles
  - Supervision des agents et réaffectation si besoin
- **Interface dédiée** :
  - Dashboard global (statistiques, graphiques)
  - Gestion des utilisateurs et des droits
  - Accès à toutes les fonctionnalités agent et client en lecture seule

---

## 1. Objectif
Ce document décrit comment intégrer la gestion des crédits (demandes, simulation, échéancier, paiement) dans le frontend Angular et détaille le plan d’implémentation.

---

## 2. Fonctionnalités à développer

### A. Simulation de crédit
- **But** : Permettre à l’utilisateur de simuler un crédit (montant, taux, durée, type)
- **API** : `GET /api/credits/simulate`
- **Affichage** : Résultat du calcul (mensualités, intérêts, échéancier)

### B. Création de demande de crédit
- **But** : Permettre à l’utilisateur de soumettre une demande de crédit
- **API réelle backend** : `POST /api/credits/with-health-report` (multipart)
- **Données (multipart)** :
  - `amountRequested` (number)
  - `durationMonths` (number)
  - `typeCalcul` (`AMORTISSEMENT_CONSTANT` | `ANNUITE_CONSTANTE`)
  - `healthReport` (File) **obligatoire** (PDF ou image)
- **Gestion du JWT** : Authentification obligatoire

### C. Suivi des demandes
- **But** : Afficher la liste et le statut des crédits de l’utilisateur
- **API** :
  - **ADMIN** : `GET /api/credits` (liste globale)
  - **AGENT** : `GET /api/credits/pending` (crédits en attente)
  - **Tous rôles** : `GET /api/credits/{id}` (détail)
- **Limite backend (CLIENT)** : il n’y a pas d’endpoint “mes crédits”. Le frontend fait donc : création → redirection vers le détail du crédit créé.
- **Affichage** : Statut, montant, date, etc.

### D. Affichage du tableau d’amortissement
- **But** : Afficher le détail des échéances d’un crédit validé
- **API** : `GET /api/credits/{id}/schedule` ou `/api/repayments/credit/{creditId}`

### E. Paiement d’une échéance
- **But** : Permettre à l’utilisateur de payer une mensualité
- **API** : `PATCH /api/repayments/{id}/pay`

---

## 3. Plan d’implémentation

### 1. Composants + service (standalone)
- Composants ajoutés :
  - `CreditSimulateComponent`
  - `CreditRequestNewComponent`
  - `CreditDetailComponent` (inclut échéancier + paiements)
  - `CreditListComponent` (ADMIN/AGENT)
- Service + modèles :
  - `CreditApiService`
  - `credit-api.model.ts`

### 2. Routing
- Routes (dans `/dashboard`) :
  - `/dashboard/credit` → Liste (ADMIN/AGENT) ou page d’accès client (CLIENT)
  - `/dashboard/credit/simulate` → Simulation
  - `/dashboard/credit/new` → Nouvelle demande (multipart)
  - `/dashboard/credit/:id` → Détail + échéancier + paiements

### 3. Sécurité
- Gérer le JWT dans les requêtes HTTP (intercepteur Angular)
- Rediriger vers login si non authentifié

### 4. UX/UI
- Utiliser Angular Material ou Bootstrap pour les formulaires et tableaux
- Afficher les messages d’erreur et de succès
- Prévoir des loaders/spinners

---

## 4. Ce qu’il faut faire (TODO)

- [x] Créer les composants (standalone) + routing
- [x] Développer le service Angular pour les appels API
- [x] Intégrer la simulation de crédit
- [x] Intégrer la création de demande (multipart + fichier)
- [x] Afficher la liste des crédits (ADMIN) / crédits en attente (AGENT)
- [x] Afficher le tableau d’amortissement (dans le détail)
- [x] Gérer le paiement d’échéance (dans le détail)
- [x] Sécuriser les routes et appels API (JWT déjà présent via intercepteur)
- [x] Compiler l’app (ng build)

Notes :
- La “liste des crédits du client” dépend d’un endpoint backend manquant (ex: `GET /api/credits/my`).

---

## 5. Conseils
- Bien lire les réponses d’API pour adapter l’affichage
- Toujours vérifier le statut du crédit avant d’afficher l’échéancier
- Gérer les erreurs (ex : crédit non validé, paiement déjà effectué, etc.)
- Utiliser les modèles/dto Angular pour le typage

---

## 6. Pour aller plus loin
- Ajouter des notifications (ex : échéance à venir)
- Permettre l’upload de documents (rapport médical)
- Ajouter un dashboard de suivi

---

**Contactez le backend pour toute question sur les endpoints ou les données attendues.**
