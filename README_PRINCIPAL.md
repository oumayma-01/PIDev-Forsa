# 🏦 FORSA - Système de Gestion de Crédits Bancaires

> Plateforme complète de gestion de crédits avec IA, assignation d'agents, système de gifts et pénalités automatiques

[![Java](https://img.shields.io/badge/Java-17-orange.svg)](https://openjdk.org/)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.0.4-brightgreen.svg)](https://spring.io/projects/spring-boot)
[![MySQL](https://img.shields.io/badge/MySQL-8.0-blue.svg)](https://www.mysql.com/)
[![License](https://img.shields.io/badge/License-MIT-yellow.svg)](LICENSE)

---

## 📋 Table des Matières

- [Vue d'ensemble](#-vue-densemble)
- [Fonctionnalités](#-fonctionnalités)
- [Architecture](#-architecture)
- [Technologies](#-technologies)
- [Installation](#-installation)
- [Configuration](#%EF%B8%8F-configuration)
- [Utilisation](#-utilisation)
- [API Documentation](#-api-documentation)
- [Tests](#-tests)
- [Contribution](#-contribution)
- [License](#-license)

---

## 🎯 Vue d'ensemble

**FORSA** est une application Spring Boot professionnelle de gestion de crédits bancaires qui intègre :

- 🤖 **Analyse IA unifiée** (scoring de risque + évaluation d'assurance médicale)
- 👥 **Assignation automatique d'agents** avec gestion du statut (busy/available)
- 🎁 **Système de rewards** (gifts cumulatifs avec attribution automatique)
- 💰 **Pénalités automatiques** pour retards de paiement
- 📊 **Calculs d'échéances** (2 stratégies : annuité constante / amortissement constant)
- 🔒 **Authentification JWT** sécurisée
- 📄 **Upload de documents** (rapports médicaux pour assurance)

---

## ✨ Fonctionnalités

### 1. Gestion des Crédits

- **Création de demande** avec upload de rapport médical (multipart)
- **Analyse IA automatique** : scoring fraude + évaluation assurance
- **Workflow complet** : SOUMISSION → ANALYSE → ASSIGNATION AGENT → VALIDATION → ACTIVATION
- **Calculs BigDecimal** avec arrondis HALF_EVEN
- **Strategy Pattern** pour méthodes de calcul :
  - Annuité constante (mensualité fixe)
  - Amortissement constant (capital constant)

### 2. Assignation d'Agents

- **Sélection automatique** d'un agent disponible lors de la création
- **Flag busy** : un agent ne peut traiter qu'une seule demande à la fois
- **Verrou pessimiste** pour éviter les conflits de concurrence
- **Libération automatique** ou manuelle des agents

### 3. Système de Gifts

- **Accumulation automatique** de 1.5% du capital de chaque crédit approuvé
- **Attribution automatique** quand le montant cumulé atteint 500 DT
- **Réinitialisation** après attribution pour permettre de nouvelles accumulations
- **Traçabilité complète** : dates, montants, historique

### 4. Pénalités Automatiques

- **Scheduler quotidien** (1h du matin) vérifiant les retards
- **Pénalité fixe** de 200 DT ajoutée comme ligne PENALTY dans le schedule
- **Mise à jour automatique** du remainingBalance du crédit
- **Différenciation** : lineType NORMAL vs PENALTY

### 5. Sécurité

- **JWT (HS512)** pour l'authentification
- **Rôles** : CLIENT, AGENT, ADMIN
- **Endpoints protégés** avec @PreAuthorize
- **Validation** des inputs et gestion d'erreurs

---

## 🏗️ Architecture

### Architecture Globale

```
┌─────────────────────────────────────────────────────────────┐
│                     CLIENT (Frontend)                        │
└────────────────────┬────────────────────────────────────────┘
                     │ HTTP/REST (JWT)
┌────────────────────▼────────────────────────────────────────┐
│              SPRING BOOT API (Port 8089)                    │
│  ┌──────────────────────────────────────────────────────┐   │
│  │  Controllers (Credit, Agent, Gift, Penalty, Auth)    │   │
│  └────────────┬─────────────────────────────────────────┘   │
│               │                                              │
│  ┌────────────▼─────────────────────────────────────────┐   │
│  │  Services (Business Logic)                           │   │
│  │  - CreditRequestService                              │   │
│  │  - AgentAssignmentService                            │   │
│  │  - GiftService                                       │   │
│  │  - PenaltyService                                    │   │
│  │  - UnifiedCreditAnalysisService                      │   │
│  └────────────┬─────────────────────────────────────────┘   │
│               │                                              │
│  ┌────────────▼─────────────────────────────────────────┐   │
│  │  Repositories (JPA)                                  │   │
│  └────────────┬─────────────────────────────────────────┘   │
└───────────────┼──────────────────────────────────────────────┘
                │
┌───────────────▼──────────────────────────────────────────────┐
│                    MySQL Database                             │
│  Tables: credit_request, agent, gift, repayment_schedule,... │
└───────────────────────────────────────────────────────────────┘

                │ HTTP/REST
┌───────────────▼──────────────────────────────────────────────┐
│              API IA UNIFIÉE (Port 8000)                       │
│  - Scoring fraude (ML)                                        │
│  - Analyse assurance médicale (OCR + LLM)                     │
│  - Génération rapports PDF                                    │
└───────────────────────────────────────────────────────────────┘
```

### Pattern Architecture

- **3-Tier Architecture** : Controller → Service → Repository
- **Strategy Pattern** : Calculs d'amortissement (AnnuiteConstante, AmortissementConstant)
- **Repository Pattern** : Abstraction de la couche data
- **DTO Pattern** : Séparation entités/DTOs pour API
- **Builder Pattern** : Construction d'objets complexes

---

## 🛠️ Technologies

### Backend
- **Java 17**
- **Spring Boot 3.0.4**
  - Spring Web
  - Spring Security
  - Spring Data JPA
  - Spring Scheduling
- **JWT (io.jsonwebtoken:jjwt)**
- **MySQL 8.0**
- **Maven**

### Frontend (non inclus dans ce repo)
- Angular / React / Vue.js
- Bootstrap / Material UI

### IA / ML
- **Python 3.10+**
- **FastAPI**
- **Scikit-learn** (scoring fraude)
- **Tesseract OCR** (extraction rapport médical)
- **LM Studio** (analyse LLM)

---

## 🚀 Installation

### Prérequis

- Java 17+
- MySQL 8.0+
- Maven 3.6+
- Git

### Étapes

1. **Cloner le repository**
   ```bash
   git clone https://github.com/votre-org/PIDev-Forsa.git
   cd PIDev-Forsa
   ```

2. **Configurer la base de données**
   ```sql
   CREATE DATABASE forsa_db;
   ```

3. **Créer les tables** (voir `GUIDE_TESTS_COMPLET.md` section "Créer les tables")
   ```sql
   -- Exécuter les scripts SQL pour créer les tables
   -- agent, gift, et ajouter line_type à repayment_schedule
   ```

4. **Configurer application.properties**
   ```properties
   # Copier application.properties.example vers application.properties
   # Ajuster les paramètres DB et JWT secret
   ```

5. **Compiler le projet**
   ```bash
   ./mvnw clean compile
   ```

6. **Lancer l'application**
   ```bash
   ./mvnw spring-boot:run
   ```

L'application démarre sur **http://localhost:8089**

---

## ⚙️ Configuration

### application.properties (extrait)

```properties
# Server
server.port=8089
server.servlet.context-path=/forsaPidev

# Database
spring.datasource.url=jdbc:mysql://localhost:3306/forsa_db
spring.datasource.username=root
spring.datasource.password=your_password

# JPA
spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=true

# JWT
jwt.secret=VotreCleSecreteTresLongueEtSecurisee
jwt.expirationMs=31536000000

# IA Service
ai.scoring.base-url=http://localhost:8000
ai.scoring.connect-timeout-ms=15000
ai.scoring.read-timeout-ms=120000

# File Upload
spring.servlet.multipart.max-file-size=10MB
spring.servlet.multipart.max-request-size=10MB
upload.path=uploads/health-reports/

# Scheduler (Penalties)
penalty.amount=200.00
penalty.scheduler.enabled=true
```

---

## 📖 Utilisation

### 1. Créer un utilisateur

```bash
curl -X POST "http://localhost:8089/forsaPidev/api/auth/signup" \
  -H "Content-Type: application/json" \
  -d '{
    "username": "testclient",
    "email": "client@test.com",
    "password": "Test@1234",
    "role": "CLIENT"
  }'
```

### 2. Se connecter

```bash
curl -X POST "http://localhost:8089/forsaPidev/api/auth/signin" \
  -H "Content-Type: application/json" \
  -d '{
    "username": "testclient",
    "password": "Test@1234"
  }'
```

**Réponse** : token JWT à utiliser dans les requêtes suivantes

### 3. Créer une demande de crédit

```bash
curl -X POST "http://localhost:8089/forsaPidev/api/credits/with-health-report" \
  -H "Authorization: Bearer <token>" \
  -F "amountRequested=100000" \
  -F "durationMonths=12" \
  -F "typeCalcul=AMORTISSEMENT_CONSTANT" \
  -F "healthReport=@health_report.pdf"
```

### 4. Approuver (en tant qu'agent)

```bash
curl -X POST "http://localhost:8089/forsaPidev/api/credits/85/approve" \
  -H "Authorization: Bearer <agent_token>"
```

**➡️ Voir la documentation complète des endpoints :** [API_ENDPOINTS_DOCUMENTATION.md](API_ENDPOINTS_DOCUMENTATION.md)

---

## 📚 API Documentation

### Swagger UI

Une fois l'application lancée, accédez à :

**http://localhost:8089/forsaPidev/swagger-ui.html**

### Endpoints Principaux

| Méthode | Endpoint | Description | Rôle requis |
|---------|----------|-------------|-------------|
| POST | `/api/auth/signin` | Connexion | PUBLIC |
| POST | `/api/credits/with-health-report` | Créer demande + rapport | CLIENT |
| POST | `/api/credits/{id}/approve` | Approuver | AGENT/ADMIN |
| GET | `/api/credits/{id}/schedule` | Voir échéances | CLIENT/AGENT |
| GET | `/api/gifts/client/{id}` | Voir gift accumulé | CLIENT |
| POST | `/api/agents/{id}/release` | Libérer agent | ADMIN |

**➡️ Documentation complète :** [API_ENDPOINTS_DOCUMENTATION.md](API_ENDPOINTS_DOCUMENTATION.md)

---

## 🧪 Tests

### Tests Unitaires

```bash
./mvnw test
```

### Tests d'Intégration

```bash
./mvnw verify
```

### Tests Manuels

Voir le guide complet : **[GUIDE_TESTS_COMPLET.md](GUIDE_TESTS_COMPLET.md)**

Exemples de tests :
- ✅ Création de crédit avec rapport médical
- ✅ Assignation automatique d'agent
- ✅ Accumulation et attribution de gift
- ✅ Application automatique de pénalités
- ✅ Génération de tableaux d'amortissement

---

## 📊 Rapport d'Implémentation

**Statut actuel :** ✅ **IMPLÉMENTATION COMPLÈTE**

### Fonctionnalités Implémentées

- [x] Endpoint multipart upload rapport médical
- [x] Appel API IA unifiée (fraude + assurance)
- [x] Assignation automatique d'agents
- [x] Flag busy sur agents
- [x] Accumulation gift 1.5% par crédit
- [x] Attribution automatique gift ≥ 500 DT
- [x] Scheduler pénalités quotidien (1h du matin)
- [x] Pénalité fixe 200 DT
- [x] Ligne PENALTY dans schedule
- [x] Strategy Pattern pour calculs
- [x] BigDecimal + HALF_EVEN partout
- [x] Sécurité JWT robuste
- [x] Logging détaillé

**➡️ Rapport détaillé :** [RAPPORT_IMPLEMENTATION_COMPLETE.md](RAPPORT_IMPLEMENTATION_COMPLETE.md)

---

## 📁 Structure du Projet

```
PIDev-Forsa/
├── src/
│   ├── main/
│   │   ├── java/org/example/forsapidev/
│   │   │   ├── Controllers/
│   │   │   │   ├── CreditRequestController.java
│   │   │   │   ├── AuthController.java
│   │   │   │   └── ...
│   │   │   ├── Services/
│   │   │   │   ├── CreditRequestService.java
│   │   │   │   ├── AgentAssignmentService.java
│   │   │   │   ├── GiftService.java
│   │   │   │   ├── PenaltyService.java
│   │   │   │   └── ...
│   │   │   ├── Repositories/
│   │   │   │   ├── CreditRequestRepository.java
│   │   │   │   ├── AgentRepository.java
│   │   │   │   ├── GiftRepository.java
│   │   │   │   └── ...
│   │   │   ├── entities/
│   │   │   │   ├── CreditManagement/
│   │   │   │   │   ├── CreditRequest.java
│   │   │   │   │   ├── RepaymentSchedule.java
│   │   │   │   │   ├── Gift.java
│   │   │   │   │   ├── LineType.java
│   │   │   │   │   └── ...
│   │   │   │   ├── UserManagement/
│   │   │   │   │   ├── User.java
│   │   │   │   │   ├── Agent.java
│   │   │   │   │   └── ...
│   │   │   ├── security/
│   │   │   │   ├── WebSecurityConfig.java
│   │   │   │   ├── jwt/
│   │   │   │   └── ...
│   │   │   └── ForsaPidevApplication.java
│   │   └── resources/
│   │       ├── application.properties
│   │       └── ...
│   └── test/
├── uploads/
│   └── health-reports/
├── FAIRE_ME.md (Plan d'implémentation)
├── RAPPORT_IMPLEMENTATION_COMPLETE.md
├── GUIDE_TESTS_COMPLET.md
├── API_ENDPOINTS_DOCUMENTATION.md
├── pom.xml
└── README.md (ce fichier)
```

---

## 🤝 Contribution

Les contributions sont les bienvenues ! Veuillez suivre ces étapes :

1. Fork le projet
2. Créer une branche feature (`git checkout -b feature/AmazingFeature`)
3. Commit vos changements (`git commit -m 'Add AmazingFeature'`)
4. Push vers la branche (`git push origin feature/AmazingFeature`)
5. Ouvrir une Pull Request

### Guidelines

- Respecter les conventions de nommage Java
- Ajouter des tests unitaires pour toute nouvelle fonctionnalité
- Documenter les endpoints dans Swagger
- Utiliser BigDecimal pour les calculs monétaires
- Ajouter du logging approprié

---

## 📝 License

Ce projet est sous licence MIT - voir le fichier [LICENSE](LICENSE) pour plus de détails.

---

## 👨‍💻 Auteurs

- **Équipe PIDev** - *Développement initial*

---

## 🙏 Remerciements

- Spring Boot Team pour l'excellent framework
- Équipe FastAPI pour l'API Python
- Communauté open source

---

## 📞 Support

Pour toute question ou problème :

- 📧 Email : support@forsa.com
- 📚 Documentation : [GUIDE_TESTS_COMPLET.md](GUIDE_TESTS_COMPLET.md)
- 🐛 Issues : [GitHub Issues](https://github.com/votre-org/PIDev-Forsa/issues)

---

## 🔮 Roadmap

### Version 1.1 (Q2 2026)
- [ ] Notifications email/SMS
- [ ] Dashboard administrateur
- [ ] Export Excel/PDF des rapports
- [ ] API REST versioning

### Version 2.0 (Q3 2026)
- [ ] Multi-devises
- [ ] Workflow d'approbation multi-niveaux
- [ ] Intégration PSP (Stripe, PayPal)
- [ ] Module de prédiction ML avancé

---

**Version actuelle :** 1.0.0  
**Dernière mise à jour :** 2026-03-02  
**Statut :** ✅ Production Ready

---

<div align="center">
  Made with ❤️ by FORSA Team
</div>

