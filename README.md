# ForsaPidev Backend

## Overview

ForsaPidev is a backend-only Spring Boot application designed for credit management and intelligent decision support. It provides a secure REST API for handling credit requests, repayment schedules, agent assignment, insurance analysis, and AI-powered scoring features. The project integrates with MySQL for persistence and uses Hibernate/JPA for data access.

The backend is built to support banking-style workflows, including:
- credit request creation and validation,
- automatic repayment schedule generation,
- agent review and decision handling,
- health-report-based insurance analysis,
- AI risk scoring and feature extraction.

## Features

- Credit request management with approval workflow
- Monthly repayment schedule generation
- Automatic assignment of credit requests to available agents
- AI-based credit risk analysis
- Health-report upload and insurance analysis
- Support for intelligent scoring features used by the AI model
- JWT-based authentication and role-based authorization
- MySQL persistence with Hibernate/JPA
- Swagger/OpenAPI documentation for backend endpoints
- Scheduled tasks for agent reassignment and monitoring

## Tech Stack

### Backend Only

- Java 17
- Spring Boot 3
- Spring Web
- Spring Security
- Spring Data JPA / Hibernate
- MySQL
- JWT Authentication
- Swagger / OpenAPI
- Maven
- Multipart file upload support
- Scheduled background jobs
- RESTful architecture

## Architecture

The application follows a layered backend architecture:

- **Controller Layer**: Exposes REST endpoints for credits, authentication, agents, insurance, and scoring.
- **Service Layer**: Contains business logic for credit processing, agent assignment, repayment generation, AI scoring, and insurance analysis.
- **Repository Layer**: Handles database access through Spring Data JPA.
- **Entity Layer**: Models domain concepts such as users, agents, credit requests, repayment schedules, and scoring entities.
- **AI Integration Layer**: Prepares features, sends them to the AI service, and stores the returned prediction/scoring results.

### Main Backend Workflows

1. **Credit Request Creation**
   - A client submits a credit request.
   - The backend stores the request with an initial status.
   - AI scoring and feature extraction can enrich the request.
   - The system assigns the request to an available agent if one exists.

2. **Agent Assignment**
   - The backend checks for active and available agents.
   - If an agent is free, the credit request is assigned and marked under review.
   - If no agent is available, the request stays submitted until reassignment becomes possible.

3. **Risk and AI Analysis**
   - The backend calculates and prepares features required by the AI model.
   - The AI service returns risk-related outputs such as score, risk level, and decision signals.

4. **Insurance Analysis**
   - Clients upload a health report.
   - The backend forwards the report to the AI service for insurance assessment.
   - The returned insurance rate is used in the credit analysis workflow.

5. **Repayment Schedule Generation**
   - After approval, the backend generates a monthly amortization table.
   - Schedules are persisted and used to track repayment state.

## Contributors

- **Main Developer**: Your Name
- **Academic Supervisor / Reviewer**: To be filled in
- **Project Team Members**: To be filled in if applicable

## Academic Context

This project was developed as part of an academic work focused on backend engineering, credit management, and AI-assisted decision support. It demonstrates practical use of:

- enterprise backend design,
- secure API development,
- database-driven business logic,
- AI feature preparation and scoring integration,
- modular service-oriented architecture.

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

