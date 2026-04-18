# Méthodes Statistiques - Frontend Implementation

## Vue d'ensemble des statistiques

Les méthodes statistiques sont implémentées à **deux niveaux**:

### 1️⃣ **Niveau Service (Backend Calls)**

Les services appellent les endpoints du backend qui calculent les statistiques:

```typescript
// complaint.service.ts
getSummaryReport()       // GET /api/complaints/summary-report
getTrendsLastMonths(6)   // GET /api/complaints/trends-last-months?months=6
getStatsByCategory()     // GET /api/complaints/stats-by-category
getStatsByPriority()     // GET /api/complaints/stats-by-priority

// feedback.service.ts
getSummaryReport()       // GET /api/feedbacks/report/summary
getTrendsReport(6)       // GET /api/feedbacks/report/trends?months=6
getAvgRatingByCategory() // GET /api/feedbacks/report/avg-rating-by-category
```

---

## 2️⃣ **Niveau Composant (Frontend Rendering)**

### **Component: feedback-stats.component.ts**

```typescript
export class FeedbackStatsComponent implements OnInit {
  // 🎯 Objets stockant les données brutes du backend
  summaryReport: any = null;      // {totalComplaints, openComplaints, ...}
  statsByCategory: any = null;    // {TECHNICAL: 18, FINANCE: 14, ...}
  statsByPriority: any = null;    // {CRITICAL: 10, HIGH: 8, ...}
  feedbackSummary: any = null;    // {totalCount, averageRating, ...}
  avgRatingByCategory: any = null;// {TECHNICAL: 4.2, FINANCE: 3.8, ...}

  // ✅ Helper methods pour extraire/formatter les données
  getTotalComplaints(): number {
    return this.summaryReport?.totalComplaints ?? 0;
  }

  getOpenComplaints(): number {
    return this.summaryReport?.openComplaints ?? 0;
  }

  getResolvedComplaints(): number {
    return this.summaryReport?.resolvedComplaints ?? 0;
  }

  // 🔄 Méthode pour obtenir les clés d'une statistique
  getCategoryKeys(): string[] {
    return this.statsByCategory ? Object.keys(this.statsByCategory) : [];
  }

  // 📊 Obtenir la valeur pour une catégorie
  getCategoryValue(category: string): number {
    return this.statsByCategory?.[category] ?? 0;
  }

  // 📈 Calcul du pourcentage pour la barre de progression
  // Utilisé en template: (getCategoryValue(category) / getTotalComplaints()) * 100
}
```

---

## 3️⃣ **Implémentation en Template**

### **Cartes Statistiques (Metric Cards)**

```html
<!-- Affiche les chiffres clés -->
<div class="stats-grid">
  <app-forsa-card class="stat-card">
    <div class="stat-content">
      <div class="stat-icon" style="background: #3b82f6;">
        <app-forsa-icon name="alert-circle" [size]="24" />
      </div>
      <div>
        <p class="stat-label">Total Complaints</p>
        <!-- Appel de la méthode du composant -->
        <p class="stat-value">{{ getTotalComplaints() }}</p>
      </div>
    </div>
  </app-forsa-card>
</div>
```

### **Barres de Progression (Progress Bars)**

```html
<!-- Affiche la distribution par catégorie -->
@for (category of getCategoryKeys(); track category) {
  <div class="table-row">
    <span class="table-label">{{ category }}</span>
    <div class="progress-bar">
      <!-- Calcul dynamique de la largeur -->
      <div
        class="progress-fill"
        [style.width.%]="
          (getCategoryValue(category) / (getTotalComplaints() || 1)) * 100
        "
      ></div>
    </div>
    <span class="table-value">{{ getCategoryValue(category) }}</span>
  </div>
}
```

**Calcul du pourcentage:**
```
Largeur % = (Count pour catégorie / Total) × 100

Exemple:
- TECHNICAL: 18 complaints
- FINANCE: 14 complaints  
- Total: 50 complaints

TECHNICAL: (18 / 50) × 100 = 36%  → Barre remplie à 36%
FINANCE:   (14 / 50) × 100 = 28%  → Barre remplie à 28%
```

### **Tableau des Ratings par Catégorie**

```html
@if (avgRatingByCategory) {
  <div class="stats-section">
    <h3>Average Rating by Category</h3>
    <div class="rating-grid">
      @for (category of getCategoryKeys(); track category) {
        <div class="rating-card">
          <p class="rating-category">{{ category }}</p>
          <!-- Pipe number pour formater: 4.23 → 4.2 -->
          <p class="rating-score">
            {{ (avgRatingByCategory[category] | number: '1.1-1') ?? 'N/A' }}
          </p>
        </div>
      }
    </div>
  </div>
}
```

---

## 4️⃣ **Processus de Chargement des Données**

```typescript
loadAllStats(): void {
  this.loading = true;  // 🔄 Affiche le spinner
  this.error = '';

  // 1️⃣ Appel au service
  this.complaintService.getSummaryReport().subscribe({
    next: (data) => {
      this.summaryReport = data;  // 💾 Stock les données
      // Composant se re-render automatiquement
    },
    error: () => {
      this.error = 'Error loading summary report';
    }
  });

  // 2️⃣ Appels parallèles aux autres endpoints
  this.complaintService.getStatsByCategory().subscribe({
    next: (data) => {
      this.statsByCategory = data;  // {TECHNICAL: 18, FINANCE: 14, ...}
    },
    error: () => console.error('Error');
  });

  // 3️⃣ Dernier appel désactive le loading
  this.complaintService.getStatsByPriority().subscribe({
    next: (data) => {
      this.statsByPriority = data;
      this.loading = false;  // ✅ Masque le spinner
    },
    error: () => this.loading = false;
  });
}
```

---

## 5️⃣ **Flux de Données Visuel**

```
┌────────────────────────────────────────────────────────┐
│          Backend (Spring Boot)                          │
│  ✅ Calcule les statistiques avec aggrégations         │
│  ✅ Retourne JSON structuré                            │
└────────────────────┬─────────────────────────────────┘
                     │ HTTP GET
                     ↓
┌────────────────────────────────────────────────────────┐
│   Frontend Services (complaint.service.ts)              │
│  • getSummaryReport()                                  │
│  • getStatsByCategory()                               │
│  • getStatsByPriority()                               │
│  • getTrendsLastMonths()                              │
└────────────────────┬─────────────────────────────────┘
                     │ Observable<any>
                     ↓
┌────────────────────────────────────────────────────────┐
│   Component (feedback-stats.component.ts)               │
│  • summaryReport: any                                  │
│  • statsByCategory: any                               │
│  • getTotalComplaints()                               │
│  • getCategoryValue(category)                         │
│  • Helper methods pour calculs simples               │
└────────────────────┬─────────────────────────────────┘
                     │ Component Methods
                     ↓
┌────────────────────────────────────────────────────────┐
│   Template (feedback-stats.component.html)             │
│  • Affiche les cartes statistiques                    │
│  • Barre de progression: (value / total) * 100       │
│  • Formats les nombres avec pipes                    │
│  • @for @if control flow                             │
└────────────────────────────────────────────────────────┘
                     │
                     ↓
              📊 UI Utilisateur
```

---

## 6️⃣ **Exemples de Données Retournées**

### **Summary Report**
```javascript
{
  "totalComplaints": 50,
  "openComplaints": 15,
  "resolvedComplaints": 30,
  "closedComplaints": 5,
  "rejectedComplaints": 0,
  "avgResolutionTime": 3.5,
  "satisfactionScore": 4.2
}
```

### **Stats by Category**
```javascript
{
  "TECHNICAL": 18,
  "FINANCE": 14,
  "SUPPORT": 12,
  "FRAUD": 3,
  "ACCOUNT": 2,
  "CREDIT": 1,
  "OTHER": 0
}
```

### **Stats by Priority**
```javascript
{
  "CRITICAL": 10,
  "HIGH": 8,
  "MEDIUM": 18,
  "LOW": 14
}
```

### **Feedback Summary**
```javascript
{
  "totalCount": 120,
  "averageRating": 4.2,
  "ratingDistribution": {
    "1": 5,
    "2": 8,
    "3": 18,
    "4": 45,
    "5": 44
  },
  "anonymousCount": 25
}
```

### **Average Rating by Category**
```javascript
{
  "TECHNICAL": 4.2,
  "FINANCE": 3.8,
  "SUPPORT": 4.5,
  "FRAUD": 2.1,
  "ACCOUNT": 4.0,
  "CREDIT": 3.9,
  "OTHER": 3.5
}
```

---

## 7️⃣ **Opérations Mathématiques dans le Frontend**

### **Pourcentage pour Barre de Progression**
```html
[style.width.%]="(getCategoryValue(category) / getTotalComplaints() || 1) * 100"

Exemple: (18 / 50) * 100 = 36%
```

### **Moyenne (Rating)**
```typescript
// Backend retourne déjà la moyenne
getAverageFeedbackRating(): number {
  return this.feedbackSummary?.averageRating ?? 0;
}

// Format avec pipe: 4.234 → 4.2
{{ (avgFeedbackRating | number: '1.1-1') }}
```

### **Comptage et Agrégation**
```typescript
// Tous les comptages se font CÔTÉ BACKEND
// Le frontend ne fait que:
// 1. Recevoir les données
// 2. Les afficher
// 3. Formater l'affichage
```

---

## 8️⃣ **Points Clés de l'Implémentation**

| Aspect | Implémentation |
|--------|-----------------|
| **Où se font les calculs** | Backend (Spring Boot) - Plus sûr, plus rapide |
| **Rôle du Frontend** | Affichage, formatting, UX |
| **Type d'API** | GET endpoints (pas de POST) |
| **Cache** | Pas de cache (chaque refresh appelle l'API) |
| **Permissions** | Stats visibles ADMIN only |
| **Performance** | Appels parallèles, pas de watchers |
| **Format** | JSON structuré, facilement parsable |
| **Error Handling** | Try-catch via subscribe error callback |

---

## 9️⃣ **Optimisations Possibles**

### **1. Cache avec Signal**
```typescript
private statsCache = signal<StatsData | null>(null);

getStats(): Observable<StatsData> {
  if (this.statsCache()) {
    return of(this.statsCache()!);
  }
  return this.http.get<StatsData>('/stats')
    .pipe(tap(data => this.statsCache.set(data)));
}
```

### **2. Rafraîchissement Périodique**
```typescript
ngOnInit() {
  this.loadAllStats();
  setInterval(() => this.loadAllStats(), 5 * 60 * 1000); // Toutes les 5 min
}
```

### **3. Graphiques Visuels**
```html
<!-- Remplacer les barres de progression par des charts -->
<app-chart 
  [data]="statsByCategory"
  type="bar"
></app-chart>
```

---

## 🔟 **Résumé**

### Les statistiques sont implémentées ainsi:

1. **Backend** calcule les agrégations (SQL/Java)
2. **Service** appelle les endpoints statiques
3. **Composant** stocke les données reçues
4. **Helper methods** extraient et formattent
5. **Template** affiche avec calculs simples (%, divisions)
6. **Pipes** (number, date) formattent l'affichage final

**Le frontend ne traite PAS les données brutes**, il les reçoit déjà agrégées du backend et les affiche simplement. C'est une bonne architecture car:

✅ Performance (pas d'agrégation côté client)  
✅ Sécurité (calculs audités côté serveur)  
✅ Maintenabilité (logique centralisée au backend)  
✅ Scalabilité (pas de charge de traitement au client)

---

**Version:** 1.0  
**Langue:** Français  
**Date:** 2026-04-18
