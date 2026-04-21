# FORSA — Service Intelligence Artificielle

Module de scoring de crédit intelligent.

## Description

Service Python FastAPI qui calcule le score de crédit (0-1000) en combinant :
- **Règles métier** — scoring transparent sur 7 critères
- **RAG** (ChromaDB + Sentence-Transformers) — politiques BCT
- **LLM** (Mistral 7B via Ollama) — explications en français
- **OCR** (Tesseract) — lecture automatique des documents scannés (CIN, STEG, SONEDE)

## Architecture

```
Angular (4200) → Spring Boot (8089) → Python FastAPI (5000)
                                           ↓
                                   Règles + RAG + Mistral + OCR
```

## Lancer le service

```bash
cd ai-service
pip install -r requirements.txt
python 7_api_final.py
```

Service disponible sur : **http://localhost:5000**  
Documentation interactive : **http://localhost:5000/docs**

## Endpoints

| Méthode | URL | Description |
|---------|-----|-------------|
| POST | `/calculate-score` | Score d'un client (0-1000) |
| POST | `/verify-document` | OCR d'un document scanné |
| GET  | `/health` | État du service |

## Formule de score

| Critère | Points max |
|---------|-----------|
| Salaire mensuel | 400 |
| Facture STEG payée | 100 |
| Facture SONEDE payée | 100 |
| Ancienneté compte | 100 |
| Taux d'épargne (DB) | 150 |
| Balance actuelle (DB) | 75 |
| Stabilité revenus (DB) | 75 |
| **TOTAL** | **1000** |

> La CIN est obligatoire comme prérequis d'identité mais n'entre pas dans le calcul du score.

## Prérequis

- Python 3.9+
- MySQL (XAMPP) avec base `ForsaBD`
- Ollama + modèle Mistral : `ollama pull mistral`
- Tesseract-OCR installé sur le système
