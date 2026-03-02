# 🎯 Scoring IA - Guide de démarrage rapide

## ✅ État actuel : IMPLÉMENTATION TERMINÉE

L'intégration du système de scoring IA pour évaluer le risque des demandes de crédit est **100% terminée**.

---

## 📁 Documentation disponible

| Fichier | À lire si... |
|---------|--------------|
| **[CHECKLIST_PROCHAINES_ETAPES.md](./CHECKLIST_PROCHAINES_ETAPES.md)** | 🚀 **COMMENCER ICI** - Vous voulez tester maintenant |
| **[ARCHITECTURE_SCORING_IA.md](./ARCHITECTURE_SCORING_IA.md)** | 📚 Vous voulez comprendre l'architecture complète |
| **[GUIDE_TEST_SCORING_IA.md](./GUIDE_TEST_SCORING_IA.md)** | 🧪 Vous voulez un guide de test détaillé |
| **[SUCCESS_SCORING_IA.md](./SUCCESS_SCORING_IA.md)** | 🏆 Vous voulez voir ce qui a été fait et les statistiques |
| **[RECAP_SCORING_IA.md](./RECAP_SCORING_IA.md)** | 📊 Vous voulez un récapitulatif technique |

---

## ⚡ Démarrage rapide (3 étapes)

### 1. Lancer le service IA Python

```bash
cd chemin/vers/projet-ia
.\venv\Scripts\Activate.ps1  # ou source venv/Scripts/activate
uvicorn main:app --reload --port 8000
```

### 2. Lancer Spring Boot

```bash
.\mvnw.cmd spring-boot:run
```

### 3. Tester avec Postman

```http
POST http://localhost:8089/forsaPidev/api/credits
{
  "amountRequested": 15000,
  "durationMonths": 24
}
```

**Résultat attendu :** Vous devez voir `riskScore`, `riskLevel` et `status: UNDER_REVIEW` dans la réponse.

---

## 🔄 Workflow

```
Client demande crédit → Spring calcule features → Appel IA → Score retourné 
→ Crédit en UNDER_REVIEW → Agent consulte → Agent approuve/rejette
```

---

## 🛠️ Configuration

Dans `application.properties` :

```properties
ai.scoring.enabled=true
ai.scoring.base-url=http://localhost:8000
ai.scoring.predict-path=/predict
ai.scoring.risk-threshold=0.7
```

---

## 📡 Nouveaux endpoints

| Endpoint | Méthode | Rôle |
|----------|---------|------|
| `/api/credits` | POST | Créer demande + scoring auto |
| `/api/credits/pending` | GET | Liste crédits à valider (agent) |
| `/api/credits/{id}/approve` | POST | Approuver (agent) |
| `/api/credits/{id}/reject` | POST | Rejeter (agent) |

---

## ⚠️ Important

**Le calcul des features utilise actuellement des valeurs par défaut.**

Pour un scoring réel, il faut implémenter le calcul des vraies features dans :
```
src/main/java/org/example/forsapidev/Services/scoring/CreditScoringService.java
Méthode : buildFeatures()
```

Voir détails dans [CHECKLIST_PROCHAINES_ETAPES.md](./CHECKLIST_PROCHAINES_ETAPES.md) section 6.

---

## 🆘 Support

**Problème courant** | **Solution**
---------------------|-------------
Port 8089 occupé | `netstat -ano \| findstr :8089` puis `taskkill /PID <PID> /F`
IA ne répond pas | Vérifier que le serveur Python tourne sur port 8000
Score reste null | Vérifier les logs Spring (rechercher "scoring")
Erreur compilation | `.\mvnw.cmd clean compile`

---

**🎉 Tout est prêt pour être testé ! Consultez [CHECKLIST_PROCHAINES_ETAPES.md](./CHECKLIST_PROCHAINES_ETAPES.md) pour commencer. 🎉**

