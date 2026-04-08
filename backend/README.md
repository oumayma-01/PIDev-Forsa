# ForsaPidev – Full Backend Platform for Smart Credit, Wallet, Insurance & Feedback Management

## Overview

ForsaPidev is a comprehensive backend platform built with Spring Boot, designed for intelligent credit management, digital wallet operations, insurance policy handling, user feedback/complaints, and AI-powered risk analysis.  
This project was developed as part of the PIDEV(Integrated Project of Development) – 4th Year IT Financial Engineering Program at **Esprit School of Engineering** (Academic Year 2025–2026).

The backend exposes a secure REST API for:
- Credit requests and approval workflows
- Repayment schedule and penalty management
- Agent assignment and workflow automation
- AI-based risk scoring and insurance analysis
- Digital wallet and transaction management
- Insurance product and policy lifecycle
- User feedback, complaints, and notification system
- User authentication, roles, and security

## Features

- **Credit Management**: Request, approval, scoring, agent assignment, repayment, penalties
- **AI Scoring**: Risk analysis, feature extraction, insurance rate prediction
- **Wallet Management**: User wallet, balance, transaction history, activity tracking, gift logic
- **Insurance Management**: Product catalog, policy application, agent review, premium calculation, claims
- **Feedback & Complaints**: User feedback, complaint submission, agent/admin review, priority stats
- **User & Role Management**: Registration, authentication (JWT & OAuth2), roles (client, agent, admin)
- **Notifications**: Email alerts for user verification, reset password requests, deadlines, penalties, and workflow events
- **Swagger/OpenAPI**: Full API documentation
- **Scheduled Tasks**: Automated agent assignment, repayment checks, notifications

## Tech Stack

### Backend
- Java 17
- Spring Boot 3
- Spring Web
- Spring Security (JWT, OAuth2)
- Spring Data JPA / Hibernate
- MySQL
- Swagger / OpenAPI
- Maven
- Multipart file upload support
- Scheduled background jobs
- RESTful architecture

### Frontend
*No frontend is included in this repository. The backend is designed to be consumed by any compatible frontend or API client.*

## Architecture

The platform follows a modular, layered backend architecture:

- **Controller Layer**: REST endpoints for credits, wallet, insurance, feedback, users, scoring, etc.
- **Service Layer**: Business logic for each domain (credit, wallet, insurance, feedback, user, AI)
- **Repository Layer**: Spring Data JPA for all entities
- **Entity Layer**: Users, agents, credits, repayments, wallets, transactions, insurance products/policies/claims, feedback, etc.
- **AI Integration Layer**: Feature calculation, scoring, insurance rate prediction
- **Notification Layer**: Email alerts, reminders, penalty notifications

### Main Backend Workflows

1. **Credit Request & Approval**
    - Client submits a credit request
    - AI scoring and feature extraction
    - Agent assignment (auto/manual)
    - Agent reviews, approves/rejects
    - Repayment schedule generated
    - Penalties applied for late payments

2. **Wallet Operations**
    - Each user has a wallet
    - Transactions: credit disbursement, repayments, penalties, gifts
    - Gift logic: 1.5% of interest accumulated, gift sent when threshold reached
    - Activity log for all wallet operations

3. **Insurance Management**
    - Product catalog (health, life, property, etc.)
    - Policy application (client)
    - Agent/admin review, approval, premium calculation
    - Claims submission and processing
    - AI-based insurance rate via health report analysis

4. **Feedback & Complaints**
    - Users submit feedback/complaints
    - Agent/admin review, prioritization
    - Stats and dashboard for complaint management
    - Notification on status change

5. **User & Security**
    - Registration, login (JWT)
    - OAuth2 login (Google)
    - Role-based access (client, agent, admin)
    - Agent table auto-synced with user role changes

6. **AI & Analytics**
    - Credit scoring: risk score, risk level, decision
    - Insurance rate prediction from health report (PDF upload)
    - Feature calculation for model input
    - Recent credit behavior, transaction analysis

7. **Notifications & Scheduling**
    - Email reminders before/after repayment deadlines
    - Penalty notification after late payment
    - Scheduled agent assignment for pending credits

## AI Features

- **Credit Scoring**: Behavioral/financial features, risk prediction, decision support
- **Insurance Analysis**: Health report OCR, AI rate calculation, policy eligibility
- **Feature Calculation**: JSON payload for AI model (see below)
- **Recent Credit Behavior**: Number of recent credit requests per client

### Example AI Input Features

```json
{
  "avg_delay_days": 1.76,
  "payment_instability": 0.09,
  "credit_utilization": 0.58,
  "monthly_transaction_count": 8,
  "transaction_amount_std": 201.17,
  "high_risk_country_transaction": 0,
  "unusual_night_transaction": 0,
  "address_changed": 0,
  "phone_changed": 0,
  "email_changed": 0,
  "country_changed": 0,
  "income_change_percentage": 10.27,
  "employment_changed": 0,
  "recent_credit_requests": 2
}
```

## Contributors

- **Academic Supervisors**: Mr Aymen Selmi , Mr Skander Saadaoui
- **Project Team Members**: Adem Ben Ali , Emna Ben Hdid , Mohamed Derbel , Lina Sahbani , Nasma Hamam , Oumayma Mrabet

## Academic Context

Developed at **Esprit School of Engineering – Tunisia**  
PIDEV – 4 INFINI 1 | 2025–2026  
This project is an academic project for the “esprit-school-of-engineering” program, tagged as “academic-project”, “esprit-PI”, and “Java/Spring Boot”.

## Getting Started

### Prerequisites
- Java 17
- Maven
- MySQL 8+
- A configured AI scoring service (if AI analysis is enabled)

### Clone the Repository
```bash
git clone https://github.com/your-username/your-repository.git
cd your-repository
```

### Database Setup
Create a MySQL database named `ForsaBD`:
```sql
CREATE DATABASE ForsaBD;
```
Then configure the database connection in `src/main/resources/application.properties`:
```properties
spring.datasource.url=jdbc:mysql://localhost:3306/ForsaBD?createDatabaseIfNotExist=true&useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true
spring.datasource.username=root
spring.datasource.password=your_password
```

### Run the Backend
Using Maven:
```bash
mvn clean spring-boot:run
```
Or using the Maven Wrapper:
```bash
./mvnw spring-boot:run
```
On Windows PowerShell:
```powershell
.\mvnw.cmd spring-boot:run
```

### Access the API
- Backend base URL: `http://localhost:8089/forsaPidev`
- Swagger UI: `http://localhost:8089/forsaPidev/swagger-ui.html`
- OpenAPI docs: `http://localhost:8089/forsaPidev/api-docs`

## Acknowledgments
- Spring Boot and the Spring ecosystem
- Hibernate and JPA for persistence
- MySQL for relational storage
- OpenAPI / Swagger for API documentation
- AI service integration for scoring and analytics
- Academic guidance and project reviewers

---
This project is intended for educational and backend engineering purposes, with a strong focus on maintainability, security, and intelligent credit decision support.
---
