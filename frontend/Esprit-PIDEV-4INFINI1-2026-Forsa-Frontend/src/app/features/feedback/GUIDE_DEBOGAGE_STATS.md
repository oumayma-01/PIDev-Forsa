# 🔧 GUIDE DE DÉBOGAGE - Statistiques

**Date:** 2026-04-19

---

## 🎯 Problème Actuel

Les statistiques affichent **0** pour les totaux alors que les catégories/priorités ont des données.

---

## 🔍 Comment Déboguer

### **Étape 1: Ouvrir la console du navigateur**

```
1. Appuyez sur F12 (ou Ctrl+Shift+J)
2. Allez à l'onglet "Console"
3. Allez sur http://localhost:4201/dashboard/feedback/stats
4. Cliquez "Refresh"
```

### **Étape 2: Vérifier les logs de débogage**

Vous verrez des messages comme:

```
🔄 Loading statistics from backend...
📊 Summary Report received: {...}
📂 Category Stats received: {...}
⭐ Priority Stats received: {...}
📝 Feedback Summary received: {...}
⭐ Rating by Category received: {...}
```

**Ou des erreurs comme:**

```
❌ Error loading summary report: Error: ...
❌ Error loading category stats: Error: ...
```

---

## 📝 Ce à Vérifier dans les Logs

### **1. Les données sont-elles reçues?**

```javascript
// ✅ BON - Data received
📊 Summary Report received: {
  totalComplaints: 43,
  openComplaints: 15,
  resolvedComplaints: 30,
  ...
}

// ❌ MAUVAIS - Pas de data ou null
📊 Summary Report received: null
📊 Summary Report received: undefined
```

### **2. Les clés sont-elles correctes?**

Si vous voyez:
```javascript
{
  "total": 43,        // ← Clé différente!
  "open": 15,
  "resolved": 30
}
```

Le problème est que le backend retourne `total` au lieu de `totalComplaints`.

**Solution:** Ajouter à `getTotalComplaints()`:
```typescript
return this.summaryReport?.total ?? this.summaryReport?.totalComplaints ?? 0;
```

---

## 📊 Section DEBUG en Bas de Page

**Vérifiez le bas de la page stats!** Il y a une section debug qui affiche les **données JSON brutes** reçues du backend.

```
🔧 DEBUG: API Response Data

📊 Summary Report (Raw JSON):
{
  "total": 43,
  "open": 15,
  ...
}

📂 Stats by Category:
{
  "TECHNICAL": 9,
  "FINANCE": 15,
  ...
}
```

---

## 🔗 Onglet Network (Important!)

Pour vérifier les appels API:

```
1. Ouvrez F12
2. Onglet "Network"
3. Rechargez la page
4. Cherchez les requêtes:
   - summary-report
   - stats-by-category
   - stats-by-priority
   - feedback (summary)
   - avg-rating-by-category
```

**Vérifiez pour chaque:**
- ✅ Status: 200 (OK)
- ✅ Response: Contient les données
- ❌ 404: Endpoint not found
- ❌ 500: Server error
- ❌ 403: Not authorized

---

## 💡 Cas Possibles

### **CAS 1: Les données arrivent mais avec clés différentes**

```javascript
Backend retourne:
{
  "total_complaints": 43,    // ← underscore!
  "open_complaints": 15
}

Frontend cherche:
totalComplaints                // ← camelCase!
```

**Fix:** Normaliser les noms de propriétés dans le service

### **CAS 2: Le backend retourne une liste au lieu d'un objet**

```javascript
// ❌ MAUVAIS - Liste
[
  { category: "TECHNICAL", count: 9 },
  { category: "FINANCE", count: 15 }
]

// ✅ BON - Objet
{
  "TECHNICAL": 9,
  "FINANCE": 15
}
```

### **CAS 3: L'endpoint n'existe pas**

```
❌ 404 Not Found
GET /api/complaints/summary-report

Vérifier dans le backend qu'il existe!
```

---

## ✅ Si Tout Est BON

Vous devriez voir:

1. **Console:** Messages 📊, 📂, ⭐ sans ❌
2. **Network:** Toutes les requêtes en 200
3. **Debug Section:** JSON valide avec chiffres
4. **Page:** Total Complaints = nombre réel (pas 0)

---

## 🛠️ Étapes de Correction

### **1. Vérifier le backend d'abord**

```bash
# Terminal du backend
curl http://localhost:8089/forsaPidev/api/complaints/summary-report

# Devrait retourner:
{
  "totalComplaints": 43,
  "openComplaints": 15,
  "resolvedComplaints": 30
}
```

### **2. Si le backend retourne autre chose**

Adapter le frontend pour matcher les clés du backend.

### **3. Ajouter les noms alternatifs**

Comme on a fait dans `getTotalComplaints()`:
```typescript
return this.summaryReport?.totalComplaints 
  ?? this.summaryReport?.total 
  ?? this.summaryReport?.count 
  ?? 0;
```

---

## 📌 Checklist de Débogage

- [ ] Ouvrir F12 et chercher les logs
- [ ] Vérifier qu'il n'y a pas de ❌ erreurs
- [ ] Vérifier l'onglet Network (status 200)
- [ ] Vérifier la Debug Section en bas (JSON visible)
- [ ] Vérifier les clés retournées par le backend
- [ ] Adapter le frontend si clés différentes

---

**Une fois fait, les statistiques afficheront les vraies données!** ✅
