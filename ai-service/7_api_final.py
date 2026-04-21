"""
FICHIER 7 : API FORSA - SCORING INTELLIGENT
=============================================
Score basé sur des RÈGLES MÉTIER RÉELLES
(pas ML avec données insuffisantes)

FORMULE DE SCORE :
- Salaire           : 350 points max
- Factures          : 200 points max
- CIN               : 50 points
- Données DB        : 300 points max
Total               : 1000 points max
"""

from fastapi import FastAPI, HTTPException, File, UploadFile, Form
from fastapi.middleware.cors import CORSMiddleware
from pydantic import BaseModel
from typing import Dict, Optional
import numpy as np
import mysql.connector
import chromadb
from sentence_transformers import SentenceTransformer
import requests
import os
import importlib.util

# Charger le service OCR depuis 6_ocr_service.py
try:
    _spec = importlib.util.spec_from_file_location("ocr_module", "6_ocr_service.py")
    _ocr_mod = importlib.util.module_from_spec(_spec)
    _spec.loader.exec_module(_ocr_mod)
    OCRService = _ocr_mod.OCRService
    ocr_service = OCRService()
    OCR_AVAILABLE = True
    print("✅ OCR (pytesseract) initialisé")
except Exception as _e:
    OCR_AVAILABLE = False
    print(f"⚠️  OCR non disponible : {_e}")

UPLOAD_DIR = "./uploads"
os.makedirs(UPLOAD_DIR, exist_ok=True)


# ==================== CONFIG ====================
DB_CONFIG = {
    'host': 'localhost',
    'user': 'root',
    'password': '',
    'database': 'ForsaBD'
}
OLLAMA_URL = "http://localhost:11434"


# ==================== FEATURE EXTRACTOR ====================
class FeatureExtractor:
    def __init__(self, db_config):
        self.conn = mysql.connector.connect(**db_config)

    def extract_db_features(self, client_id):
        client_id = int(client_id)
        return {
            'income_stability': self._calc_income_stability(client_id),
            'savings_rate':     self._calc_savings_rate(client_id),
            'account_age':      self._calc_account_age(client_id),
            'current_balance':  self._calc_current_balance(client_id),
            'activity':         self._calc_activity(client_id),
            'avg_income':       self._calc_avg_income(client_id),
        }

    def _calc_income_stability(self, client_id):
        query = """
        SELECT DATE_FORMAT(t.date, '%Y-%m') as month, SUM(t.amount)
        FROM transaction t JOIN wallet w ON t.wallet_id = w.id
        WHERE w.owner_id = %s
          AND t.type IN ('DEPOSIT', 'TRANSFER_IN')
          AND t.date >= DATE_SUB(NOW(), INTERVAL 6 MONTH)
        GROUP BY month
        """
        cursor = self.conn.cursor()
        cursor.execute(query, (client_id,))
        rows = cursor.fetchall()
        cursor.close()
        if len(rows) > 1:
            incomes = [float(r[1]) for r in rows]
            mean = np.mean(incomes)
            std = np.std(incomes)
            if mean > 0:
                return float(std / mean)
        return None  # Pas de données

    def _calc_savings_rate(self, client_id):
        query = """
        SELECT
            SUM(CASE WHEN t.type IN ('DEPOSIT','TRANSFER_IN') THEN t.amount ELSE 0 END),
            SUM(CASE WHEN t.type IN ('WITHDRAW','TRANSFER_OUT') THEN t.amount ELSE 0 END)
        FROM transaction t JOIN wallet w ON t.wallet_id = w.id
        WHERE w.owner_id = %s
          AND t.date >= DATE_SUB(NOW(), INTERVAL 6 MONTH)
        """
        cursor = self.conn.cursor()
        cursor.execute(query, (client_id,))
        row = cursor.fetchone()
        cursor.close()
        income   = float(row[0] or 0)
        spending = float(row[1] or 0)
        if income > 0:
            return (income - spending) / income
        return None

    def _calc_account_age(self, client_id):
        cursor = self.conn.cursor()
        cursor.execute(
            "SELECT DATEDIFF(NOW(), created_at)/30 FROM user WHERE id=%s",
            (client_id,)
        )
        row = cursor.fetchone()
        cursor.close()
        return float(row[0]) if row and row[0] else 0.0

    def _calc_current_balance(self, client_id):
        cursor = self.conn.cursor()
        cursor.execute("SELECT balance FROM wallet WHERE owner_id=%s", (client_id,))
        row = cursor.fetchone()
        cursor.close()
        return float(row[0]) if row and row[0] else 0.0

    def _calc_activity(self, client_id):
        query = """
        SELECT COUNT(*), DATEDIFF(MAX(t.date), MIN(t.date))+1
        FROM transaction t JOIN wallet w ON t.wallet_id=w.id
        WHERE w.owner_id=%s
        """
        cursor = self.conn.cursor()
        cursor.execute(query, (client_id,))
        row = cursor.fetchone()
        cursor.close()
        if row and row[0] and row[1]:
            return float(int(row[0]) / int(row[1]))
        return None

    def _calc_avg_income(self, client_id):
        query = """
        SELECT AVG(monthly_income) FROM (
            SELECT DATE_FORMAT(t.date,'%Y-%m') as month, SUM(t.amount) as monthly_income
            FROM transaction t JOIN wallet w ON t.wallet_id=w.id
            WHERE w.owner_id=%s
              AND t.type IN ('DEPOSIT','TRANSFER_IN')
              AND t.date >= DATE_SUB(NOW(), INTERVAL 6 MONTH)
            GROUP BY month
        ) tmp
        """
        cursor = self.conn.cursor()
        cursor.execute(query, (client_id,))
        row = cursor.fetchone()
        cursor.close()
        return float(row[0]) if row and row[0] else None

    def close(self):
        self.conn.close()


# ==================== SCORING ENGINE ====================
def calculate_intelligent_score(salary, steg_ok, sonede_ok, db_features):
    """
    FORMULE DE SCORE — CIN EXCLUE (identité = prérequis, pas facteur de score)

    - Salaire              : 400 pts max
    - Facture STEG         : 100 pts
    - Facture SONEDE       : 100 pts
    - Ancienneté compte    : 100 pts
    - Épargne (DB)         : 150 pts
    - Balance (DB)         :  75 pts
    - Stabilité revenus    :  75 pts
    TOTAL MAX              : 1000 pts
    """
    score = 0
    details = {}

    # ── 1. SALAIRE (400 points max) ──────────────────────────────
    salary = float(salary or 0)
    if salary <= 0:
        salary_points, salary_comment = 0, "Pas de salaire fourni"
    elif salary < 300:
        salary_points, salary_comment = 60, f"Salaire très bas ({salary:.0f} TND)"
    elif salary < 500:
        salary_points, salary_comment = 120, f"Salaire bas ({salary:.0f} TND)"
    elif salary < 800:
        salary_points, salary_comment = 200, f"Salaire modéré ({salary:.0f} TND)"
    elif salary < 1200:
        salary_points, salary_comment = 270, f"Bon salaire ({salary:.0f} TND)"
    elif salary < 2000:
        salary_points, salary_comment = 320, f"Très bon salaire ({salary:.0f} TND)"
    elif salary < 3000:
        salary_points, salary_comment = 370, f"Excellent salaire ({salary:.0f} TND)"
    else:
        salary_points, salary_comment = 400, f"Salaire premium ({salary:.0f} TND)"

    score += salary_points
    details['salaire'] = {'points': salary_points, 'max': 400,
                          'valeur': salary, 'comment': salary_comment}

    # ── 2. STEG (100 points) ─────────────────────────────────────
    steg_points = 100 if steg_ok else 0
    steg_comment = "Facture STEG payée à temps ✅" if steg_ok else "Facture STEG non payée ❌"
    score += steg_points
    details['steg'] = {'points': steg_points, 'max': 100, 'comment': steg_comment}

    # ── 3. SONEDE (100 points) ───────────────────────────────────
    sonede_points = 100 if sonede_ok else 0
    sonede_comment = "Facture SONEDE payée à temps ✅" if sonede_ok else "Facture SONEDE non payée ❌"
    score += sonede_points
    details['sonede'] = {'points': sonede_points, 'max': 100, 'comment': sonede_comment}

    # ── 4. ANCIENNETÉ COMPTE (100 points) ────────────────────────
    age = db_features.get('account_age', 0) or 0
    if age < 1:
        age_points, age_comment = 0, "Nouveau client (< 1 mois)"
    elif age < 3:
        age_points, age_comment = 25, f"Client récent ({age:.1f} mois)"
    elif age < 6:
        age_points, age_comment = 50, f"Client depuis {age:.1f} mois"
    elif age < 12:
        age_points, age_comment = 75, f"Client depuis {age:.1f} mois"
    else:
        age_points, age_comment = 100, f"Client fidèle ({age:.1f} mois) ✅"

    score += age_points
    details['anciennete'] = {'points': age_points, 'max': 100, 'comment': age_comment}

    # ── 6. TAUX D'ÉPARGNE DB (150 points) ────────────────────────
    savings = db_features.get('savings_rate')
    if savings is None:
        savings_points, savings_comment = 0, "Pas de données d'épargne en DB"
    elif savings < 0:
        savings_points, savings_comment = 0, "Dépenses supérieures aux revenus ❌"
    elif savings < 0.1:
        savings_points, savings_comment = 38, f"Épargne faible ({savings*100:.1f}%)"
    elif savings < 0.2:
        savings_points, savings_comment = 75, f"Épargne modérée ({savings*100:.1f}%)"
    elif savings < 0.35:
        savings_points, savings_comment = 113, f"Bonne épargne ({savings*100:.1f}%) ✅"
    else:
        savings_points, savings_comment = 150, f"Excellente épargne ({savings*100:.1f}%) ✅"

    score += savings_points
    details['epargne'] = {'points': savings_points, 'max': 150, 'comment': savings_comment}

    # ── 7. BALANCE ACTUELLE (75 points) ──────────────────────────
    balance = db_features.get('current_balance', 0) or 0
    if balance <= 0:
        balance_points, balance_comment = 0, "Balance nulle"
    elif balance < 100:
        balance_points, balance_comment = 15, f"Balance très faible ({balance:.0f} TND)"
    elif balance < 500:
        balance_points, balance_comment = 38, f"Balance modérée ({balance:.0f} TND)"
    elif balance < 2000:
        balance_points, balance_comment = 58, f"Bonne balance ({balance:.0f} TND) ✅"
    else:
        balance_points, balance_comment = 75, f"Excellente balance ({balance:.0f} TND) ✅"

    score += balance_points
    details['balance'] = {'points': balance_points, 'max': 75, 'comment': balance_comment}

    # ── 8. STABILITÉ REVENUS (75 points) ─────────────────────────
    stability = db_features.get('income_stability')
    if stability is None:
        stab_points, stab_comment = 0, "Pas de transactions en DB"
    elif stability > 1.0:
        stab_points, stab_comment = 0, "Revenus très instables ❌"
    elif stability > 0.5:
        stab_points, stab_comment = 20, "Revenus instables"
    elif stability > 0.2:
        stab_points, stab_comment = 45, "Revenus moyennement stables"
    else:
        stab_points, stab_comment = 75, "Revenus stables ✅"

    score += stab_points
    details['stabilite_revenu'] = {'points': stab_points, 'max': 75, 'comment': stab_comment}

    final_score = min(int(score), 1000)
    return final_score, details


# ==================== PYDANTIC MODELS ====================
class ScoreRequest(BaseModel):
    client_id: int
    monthly_salary: Optional[float] = 0
    steg_paid_on_time: Optional[bool] = False
    sonede_paid_on_time: Optional[bool] = False
    cin_verified: Optional[bool] = False


class ScoreResponse(BaseModel):
    client_id: int
    score: int
    score_level: str
    credit_threshold: float
    salary_verified: float
    explanation: str
    score_details: Dict
    has_db_data: bool
    features: Dict  # Compatibilité avec l'interface Angular AIScoreFeatures


# ==================== APP INIT ====================
app = FastAPI(title="FORSA AI Agent v3 - Scoring Intelligent", version="3.0.0")

app.add_middleware(
    CORSMiddleware,
    allow_origins=["*"],
    allow_methods=["*"],
    allow_headers=["*"],
)

# RAG (optionnel — fonctionne sans)
RAG_AVAILABLE = False
try:
    chroma_client   = chromadb.PersistentClient(path='./chroma_db')
    embedding_model = SentenceTransformer('sentence-transformers/all-MiniLM-L6-v2')
    policies_col    = chroma_client.get_collection("bct_policies")
    cases_col       = chroma_client.get_collection("client_cases")
    RAG_AVAILABLE   = True
    print("✅ RAG initialisé")
except Exception as e:
    print(f"⚠️  RAG non disponible (optionnel) : {e}")

print("\n🚀 API FORSA v3 - Scoring Intelligent prête sur http://localhost:5000\n")


# ==================== HELPERS ====================
def get_score_level(score):
    if score < 400: return "VERY_LOW"
    if score < 500: return "LOW"
    if score < 600: return "MEDIUM"
    if score < 700: return "GOOD"
    if score < 800: return "VERY_GOOD"
    if score < 900: return "EXCELLENT"
    return "PREMIUM"


def get_score_label(level):
    return {
        "VERY_LOW": "Très faible", "LOW": "Faible", "MEDIUM": "Moyen",
        "GOOD": "Bon", "VERY_GOOD": "Très bon",
        "EXCELLENT": "Excellent", "PREMIUM": "Premium"
    }.get(level, level)


def get_multiplier(level):
    return {
        "VERY_LOW": 0.0, "LOW": 1.0, "MEDIUM": 1.5,
        "GOOD": 2.0, "VERY_GOOD": 2.5,
        "EXCELLENT": 3.5, "PREMIUM": 4.5
    }.get(level, 0.0)


def search_rag(query):
    if not RAG_AVAILABLE:
        return {'policies': '', 'cases': ''}
    try:
        emb = embedding_model.encode(query).tolist()
        p   = policies_col.query(query_embeddings=[emb], n_results=2)
        c   = cases_col.query(query_embeddings=[emb], n_results=1)
        return {
            'policies': '\n'.join(p['documents'][0]),
            'cases':    '\n'.join(c['documents'][0])
        }
    except Exception:
        return {'policies': '', 'cases': ''}


def generate_llm_explanation(score, salary, steg_ok, sonede_ok, cin_ok,
                              score_details, rag_context, has_db_data):
    level = get_score_level(score)
    label = get_score_label(level)
    multiplier = get_multiplier(level)
    threshold = salary * multiplier

    prompt = f"""
Tu es un conseiller financier expert pour FORSA, plateforme de micro-finance tunisienne.

RÉSULTAT :
- Score : {score}/1000 — Niveau : {label}
- Salaire déclaré : {salary:.0f} TND/mois
- Seuil de crédit calculé : {threshold:,.0f} TND

DÉTAIL DU SCORE :
{chr(10).join([f"  • {v['comment']} : +{v['points']}/{v['max']} pts" for v in score_details.values()])}

DONNÉES BANCAIRES : {"Disponibles" if has_db_data else "Pas encore de données (client nouveau)"}

RÉFÉRENCES BCT :
{rag_context.get('policies', '')[:400]}

Génère en français (3-5 phrases max par section) :

**📊 VOTRE SCORE : {score}/1000 ({label})**
[Explication personnalisée du score]

**💰 SEUIL DE CRÉDIT : {threshold:,.0f} TND**
[Justification basée sur le salaire et le score]

**✅ POINTS FORTS :**
[Liste des éléments positifs]

**📈 POUR AMÉLIORER VOTRE SCORE :**
[Actions concrètes avec les points gagnables]
"""
    try:
        r = requests.post(
            f"{OLLAMA_URL}/api/generate",
            json={"model": "mistral", "prompt": prompt, "stream": False},
            timeout=120
        )
        if r.status_code == 200:
            return r.json()['response']
    except Exception as e:
        print(f"⚠️  LLM non disponible : {e}")

    # ── Fallback texte si Mistral ne répond pas ──
    positives = [v['comment'] for v in score_details.values() if v['points'] > 0]
    improvements = []
    if not steg_ok:   improvements.append("Payez vos factures STEG à temps → +100 pts")
    if not sonede_ok: improvements.append("Payez vos factures SONEDE à temps → +100 pts")
    if not has_db_data:
        improvements.append("Utilisez votre wallet FORSA régulièrement → jusqu'à +300 pts")

    return f"""**📊 VOTRE SCORE : {score}/1000 ({label})**
Votre score reflète votre profil financier actuel basé sur votre salaire, le paiement de vos factures et votre historique bancaire.

**💰 SEUIL DE CRÉDIT : {threshold:,.0f} TND**
Ce montant est calculé selon votre score et votre salaire mensuel de {salary:.0f} TND, conformément aux politiques BCT.

**✅ POINTS FORTS :**
{chr(10).join(f"• {p}" for p in positives) if positives else "• Continuez à améliorer votre profil"}

**📈 POUR AMÉLIORER VOTRE SCORE :**
{chr(10).join(f"• {i}" for i in improvements) if improvements else "• Maintenez vos bonnes habitudes financières"}"""


# ==================== ENDPOINTS ====================
@app.get("/")
def root():
    return {"message": "FORSA AI Agent v3 - Scoring Intelligent", "version": "3.0"}


@app.get("/health")
def health_check():
    status = {"api": "ok", "rag": "ok" if RAG_AVAILABLE else "unavailable"}
    try:
        conn = mysql.connector.connect(**DB_CONFIG)
        conn.close()
        status["mysql"] = "ok"
    except Exception:
        status["mysql"] = "error"
    try:
        r = requests.get(f"{OLLAMA_URL}/api/tags", timeout=3)
        status["ollama"] = "ok" if r.status_code == 200 else "error"
    except Exception:
        status["ollama"] = "error"
    return status


@app.post("/calculate-score", response_model=ScoreResponse)
def calculate_score(request: ScoreRequest):
    """
    ENDPOINT PRINCIPAL — SCORING INTELLIGENT

    Score transparent sur 1000 points.
    Fonctionne même sans données en DB (nouveaux clients).
    """
    try:
        client_id = request.client_id
        salary    = float(request.monthly_salary or 0)
        steg_ok   = bool(request.steg_paid_on_time)
        sonede_ok = bool(request.sonede_paid_on_time)
        cin_ok    = bool(request.cin_verified)

        print(f"\n🔄 Scoring client {client_id} | salary={salary} steg={steg_ok} sonede={sonede_ok} cin={cin_ok}")

        # 1. Features depuis la DB
        try:
            extractor   = FeatureExtractor(DB_CONFIG)
            db_features = extractor.extract_db_features(client_id)
            extractor.close()
            has_db_data = any(
                v is not None and v != 0
                for k, v in db_features.items()
                if k != 'account_age'
            )
        except Exception as e:
            print(f"   ⚠️  DB inaccessible : {e}")
            db_features = {}
            has_db_data = False

        # 2. Score intelligent (CIN = prérequis identité, pas dans le score)
        final_score, score_details = calculate_intelligent_score(
            salary, steg_ok, sonede_ok, db_features
        )
        print(f"   ✅ Score final : {final_score}/1000")

        # 3. Niveau + seuil
        level     = get_score_level(final_score)
        threshold = salary * get_multiplier(level)

        # 4. RAG + LLM
        rag_context = search_rag(f"score {final_score} salaire {salary} TND")
        explanation = generate_llm_explanation(
            final_score, salary, steg_ok, sonede_ok, cin_ok,
            score_details, rag_context, has_db_data
        )

        # 5. Construire features compatibles avec Angular AIScoreFeatures
        features_angular = {
            "f1_salary":           salary,
            "f2_income_stability": db_features.get('income_stability') or 0,
            "f3_savings_rate":     db_features.get('savings_rate') or 0,
            "f4_account_age":      db_features.get('account_age') or 0,
            "f5_current_balance":  db_features.get('current_balance') or 0,
            "f6_activity":         db_features.get('activity') or 0,
            "f7_avg_income":       db_features.get('avg_income') or salary,
            "f8_steg_on_time":     1 if steg_ok else 0,
            "f9_sonede_on_time":   1 if sonede_ok else 0,
            "f10_cin_verified":    1 if cin_ok else 0,
        }

        return ScoreResponse(
            client_id=client_id,
            score=final_score,
            score_level=level,
            credit_threshold=round(threshold, 2),
            salary_verified=salary,
            explanation=explanation,
            score_details=score_details,
            has_db_data=has_db_data,
            features=features_angular,
        )

    except Exception as e:
        print(f"❌ Erreur : {e}")
        import traceback
        traceback.print_exc()
        raise HTTPException(status_code=500, detail=str(e))


# ==================== ENDPOINT OCR ====================

@app.post("/verify-document")
async def verify_document(
    document_type: str = Form(...),
    file: UploadFile = File(...)
):
    """
    Vérifie un document par OCR.
    document_type : CIN | STEG | SONEDE | SALARY

    Logique de présomption :
    - STEG/SONEDE : présumé PAYÉ sauf si mot "impayé/retard" détecté
    - CIN : vérifié si numéro 8 chiffres trouvé OU mots-clés identité présents
    """
    if not OCR_AVAILABLE:
        return await _fallback_keyword_verify(document_type, file)

    try:
        # Sauvegarder le fichier
        ext = os.path.splitext(file.filename or "doc")[1] or ".jpg"
        if ext.lower() not in ('.jpg', '.jpeg', '.png', '.bmp', '.tiff', '.tif', '.webp'):
            ext = '.jpg'
        save_path = f"{UPLOAD_DIR}/verify_{document_type.upper()}{ext}"

        content = await file.read()
        with open(save_path, "wb") as f:
            f.write(content)

        print(f"\n📤 OCR request: type={document_type} file={file.filename} size={len(content)} bytes")

        result = ocr_service.extract_from_document(save_path, document_type.upper())
        print(f"📊 OCR result: {result}")
        return result

    except Exception as e:
        import traceback
        traceback.print_exc()
        raise HTTPException(status_code=500, detail=f"Erreur OCR : {str(e)}")


async def _fallback_keyword_verify(document_type: str, file: UploadFile) -> dict:
    """
    Fallback quand pytesseract n'est pas installé.
    Pour les images : on présume le document valide si l'utilisateur l'a uploadé.
    Pour les PDFs texte : on cherche des mots-clés.
    """
    import re as _re

    try:
        content = await file.read()
        # Essayer de décoder comme texte (PDF textuel ou .txt)
        text = content.decode("utf-8", errors="ignore").upper()
    except Exception:
        text = ""

    dtype = document_type.upper()
    filename_upper = (file.filename or "").upper()
    is_image = any(filename_upper.endswith(ext) for ext in
                   ['.JPG', '.JPEG', '.PNG', '.BMP', '.TIFF', '.WEBP'])

    if dtype == "CIN":
        cin_match = _re.search(r'\b\d{8}\b', text)
        has_keywords = any(kw in text for kw in ["CIN", "IDENTIT", "CARTE", "TUNISIE"])
        # Si c'est une image → on fait confiance à l'utilisateur
        verified = bool(cin_match) or has_keywords or is_image
        return {
            "document_type": "CIN",
            "verified": verified,
            "cin_number": cin_match.group() if cin_match else None,
            "name": None,
            "ocr_method": "fallback_no_tesseract",
            "note": "Tesseract non installé — vérification manuelle recommandée"
        }

    elif dtype in ("STEG", "SONEDE"):
        # Chercher explicitement "impayé" — sinon présumer payé
        has_unpaid = any(kw in text for kw in
                         ["IMPAYE", "IMPAYÉ", "RETARD", "ARRIÉRÉ", "ARRIERE",
                          "NON PAYE", "NON PAYÉ"])
        keywords_steg   = ["STEG", "ELECTRICIT", "GAZ", "ENERGIE"]
        keywords_sonede = ["SONEDE", "EAU POTABLE", "SOCIETE NATIONALE"]
        kws = keywords_steg if dtype == "STEG" else keywords_sonede
        has_doc_keywords = any(kw in text for kw in kws)

        # Présumer valide si image uploadée OU mots-clés trouvés
        verified = has_doc_keywords or is_image
        paid = verified and not has_unpaid

        return {
            "document_type": dtype,
            "verified": verified,
            "paid_on_time": paid,
            "status": "PAID" if paid else ("UNPAID" if has_unpaid else "UNKNOWN"),
            "amount": None,
            "ocr_method": "fallback_no_tesseract",
            "note": "Tesseract non installé — installer pour une vérification précise"
        }

    elif dtype == "SALARY":
        salary_match = _re.search(r'(\d{3,6})[.,]\d{1,3}', text)
        has_keywords = any(kw in text for kw in ["SALAIRE", "NET", "PAIE", "EMPLOI"])
        return {
            "document_type": "SALARY",
            "verified": has_keywords or is_image,
            "salary": float(salary_match.group(1)) if salary_match else None,
            "ocr_method": "fallback_no_tesseract"
        }

    return {"document_type": dtype, "verified": is_image, "ocr_method": "fallback_no_tesseract"}


if __name__ == "__main__":
    import uvicorn
    uvicorn.run(app, host="0.0.0.0", port=5000)
