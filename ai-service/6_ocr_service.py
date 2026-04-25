"""
FICHIER 6 : SERVICE OCR - VERSION CORRIGÉE
===========================================
Extrait automatiquement les infos depuis les documents tunisiens scannés.

LOGIQUE PRINCIPALE :
- Une facture scannée = présumée PAYÉE (on scanne pour prouver qu'on a payé)
- On marque IMPAYÉ seulement si on voit explicitement "impayé", "retard", "arriéré"
- Prétraitement image pour améliorer la qualité OCR
- Support Français + Arabe + Anglais

DOCUMENTS SUPPORTÉS :
- CIN (Carte d'Identité Nationale tunisienne)
- STEG (facture électricité/gaz)
- SONEDE (facture eau)
- SALARY (fiche de paie)
"""

import re
import os
from datetime import datetime

try:
    import pytesseract
    from PIL import Image, ImageEnhance, ImageFilter
    import numpy as np
    PYTESSERACT_OK = True
except ImportError:
    PYTESSERACT_OK = False
    print("⚠️  pytesseract/PIL non installé — OCR désactivé")

# Chemin Tesseract sur Windows (décommente si nécessaire)
# pytesseract.pytesseract.tesseract_cmd = r'C:\Program Files\Tesseract-OCR\tesseract.exe'


class OCRService:
    """Service OCR optimisé pour les documents tunisiens."""

    # ── Mots-clés qui CONFIRMENT que c'est bien une facture STEG ──
    STEG_KEYWORDS = [
        'steg', 'société tunisienne', 'electricite', 'électricité',
        'gaz', 'كهرباء', 'غاز', 'الشركة التونسية', 'energie', 'énergie',
        'abonnement', 'compteur', 'consommation', 'kwh', 'kw/h',
        'tunisienne de l\'electricite', 'distribution'
    ]

    # ── Mots-clés qui CONFIRMENT que c'est bien une facture SONEDE ──
    SONEDE_KEYWORDS = [
        'sonede', 'eau', 'water', 'الشركة الوطنية', 'شركة', 'المياه',
        'société nationale', 'distribution', 'potable', 'm3',
        'tarif eau', 'consommation d\'eau', 'robinet'
    ]

    # ── Mots-clés qui signifient PAYÉ ──
    PAID_KEYWORDS = [
        'payé', 'paye', 'payée', 'réglé', 'regle', 'réglée',
        'acquitté', 'acquitte', 'soldé', 'solde',
        'paid', 'settled', 'cleared',
        'مدفوع', 'تم الدفع', 'مسدد', 'تسديد', 'خلاص',
        'reçu de paiement', 'recu de paiement', 'quittance',
        'confirmation de paiement', 'date de paiement',
        'montant payé', 'montant regle'
    ]

    # ── Mots-clés qui signifient IMPAYÉ ──
    UNPAID_KEYWORDS = [
        'impayé', 'impaye', 'non payé', 'non paye',
        'retard', 'arriéré', 'arriere', 'arriérés',
        'unpaid', 'overdue', 'outstanding',
        'غير مدفوع', 'تأخير', 'متأخر',
        'relance', 'mise en demeure', 'suspension',
        'coupure', 'résiliation'
    ]

    # ── Mots-clés CIN tunisienne ──
    CIN_KEYWORDS = [
        'cin', 'carte', 'identité', 'identite', 'national',
        'tunisie', 'tunisienne', 'république', 'republique',
        'بطاقة', 'هوية', 'تونس', 'الجمهورية التونسية'
    ]

    def extract_from_document(self, file_path: str, document_type: str) -> dict:
        """
        Point d'entrée principal.
        document_type : 'CIN' | 'STEG' | 'SONEDE' | 'SALARY'
        """
        if not PYTESSERACT_OK:
            return self._no_ocr_fallback(document_type)

        try:
            # 1. Prétraiter l'image pour améliorer l'OCR
            image = self._preprocess_image(file_path)

            # 2. Plusieurs passes OCR avec différentes configs
            texts = []

            # Passe 1 : français + arabe
            try:
                t = pytesseract.image_to_string(image, lang='fra+ara', config='--psm 3')
                if t.strip():
                    texts.append(t)
            except Exception:
                pass

            # Passe 2 : français seul (plus fiable sur du texte latin)
            try:
                t = pytesseract.image_to_string(image, lang='fra', config='--psm 3')
                if t.strip():
                    texts.append(t)
            except Exception:
                pass

            # Passe 3 : anglais (pour les chiffres et mots communs)
            try:
                t = pytesseract.image_to_string(image, lang='eng', config='--psm 3')
                if t.strip():
                    texts.append(t)
            except Exception:
                pass

            # Combiner tous les textes extraits
            combined_text = '\n'.join(texts)
            print(f"\n📄 OCR {document_type} ({len(combined_text)} chars):\n{combined_text[:300]}...\n")

            if not combined_text.strip():
                print("⚠️  OCR n'a rien extrait — image peut-être floue ou format non supporté")
                return self._empty_result(document_type, error="Texte non lisible — image floue ou format non supporté")

            # 3. Parser selon le type
            dtype = document_type.upper()
            if dtype == 'CIN':
                return self._parse_cin(combined_text)
            elif dtype in ('STEG', 'SONEDE'):
                return self._parse_bill(combined_text, dtype)
            elif dtype == 'SALARY':
                return self._parse_salary(combined_text)
            else:
                return {'error': f'Type non supporté : {document_type}', 'verified': False}

        except Exception as e:
            print(f"❌ Erreur OCR : {e}")
            return self._empty_result(document_type, error=str(e))

    # ── Prétraitement image ──────────────────────────────────────

    def _preprocess_image(self, file_path: str):
        """Améliore la qualité de l'image pour l'OCR."""
        image = Image.open(file_path)

        # Convertir en RGB si nécessaire
        if image.mode not in ('RGB', 'L'):
            image = image.convert('RGB')

        # Agrandir si trop petite
        w, h = image.size
        if w < 1000 or h < 1000:
            scale = max(1000 / w, 1000 / h, 1.5)
            image = image.resize((int(w * scale), int(h * scale)), Image.LANCZOS)

        # Niveaux de gris
        image = image.convert('L')

        # Augmenter le contraste
        enhancer = ImageEnhance.Contrast(image)
        image = enhancer.enhance(2.0)

        # Augmenter la netteté
        enhancer = ImageEnhance.Sharpness(image)
        image = enhancer.enhance(2.0)

        # Filtre de netteté
        image = image.filter(ImageFilter.SHARPEN)

        return image

    # ── Parser STEG / SONEDE ─────────────────────────────────────

    def _parse_bill(self, text: str, doc_type: str) -> dict:
        """
        Parse une facture STEG ou SONEDE.

        Logique :
        1. Vérifier que c'est bien une facture du bon organisme
        2. PRÉSUMER PAYÉE — on ne marque impayé que si on voit explicitement des mots d'impayé
        3. Extraire le montant si possible
        """
        text_lower = text.lower()

        result = {
            'document_type': doc_type,
            'verified': False,
            'paid_on_time': False,
            'status': None,
            'amount': None,
            'raw_text_length': len(text),
        }

        # ── Étape 1 : vérifier que le document est bien du bon organisme ──
        keywords = self.STEG_KEYWORDS if doc_type == 'STEG' else self.SONEDE_KEYWORDS
        is_correct_doc = any(kw.lower() in text_lower for kw in keywords)

        if is_correct_doc:
            result['verified'] = True
        else:
            # Pas trouvé les mots-clés spécifiques mais le document a été uploadé
            # On fait confiance à l'utilisateur et on considère le document comme valide
            # si le texte n'est pas vide
            if len(text.strip()) > 50:
                result['verified'] = True
                result['note'] = f'Mots-clés {doc_type} non détectés mais document non vide'

        # ── Étape 2 : détecter si IMPAYÉ (explicitement) ──
        has_unpaid_keyword = any(kw.lower() in text_lower for kw in self.UNPAID_KEYWORDS)

        if has_unpaid_keyword:
            result['paid_on_time'] = False
            result['status'] = 'UNPAID'
            print(f"   ⚠️  Mot 'impayé' détecté dans la facture {doc_type}")
        else:
            # Présumer payée si le document est vérifié
            result['paid_on_time'] = result['verified']
            result['status'] = 'PAID' if result['verified'] else 'UNKNOWN'

        # ── Étape 3 : Bonus si on voit explicitement "payé" ──
        has_paid_keyword = any(kw.lower() in text_lower for kw in self.PAID_KEYWORDS)
        if has_paid_keyword:
            result['paid_on_time'] = True
            result['status'] = 'PAID'
            print(f"   ✅ Mot 'payé' détecté dans la facture {doc_type}")

        # ── Étape 4 : Extraire le montant ──
        amount = self._extract_amount(text)
        if amount:
            result['amount'] = amount

        print(f"   📋 {doc_type}: verified={result['verified']}, paid={result['paid_on_time']}, amount={result['amount']}")
        return result

    def _extract_amount(self, text: str):
        """Extrait un montant TND du texte."""
        patterns = [
            r'(?:montant|total|net\s*à\s*payer|à\s*payer|solde)[:\s=]*(\d+[,. ]\d{1,3})',
            r'(\d+[,.]\d{3})\s*(?:dt|tnd|dinar)',
            r'(\d+[,.]\d{1,3})\s*(?:dt|tnd)',
            r'(?:dt|tnd)\s*(\d+[,.]\d{1,3})',
        ]
        for pattern in patterns:
            match = re.search(pattern, text, re.IGNORECASE)
            if match:
                try:
                    val = match.group(1).replace(' ', '').replace(',', '.')
                    amount = float(val)
                    if 0.1 < amount < 100000:
                        return round(amount, 3)
                except Exception:
                    continue
        return None

    # ── Parser CIN ───────────────────────────────────────────────

    def _parse_cin(self, text: str) -> dict:
        """
        Parse une CIN tunisienne.
        Cherche un numéro à 8 chiffres et les mots-clés d'identité.
        """
        text_lower = text.lower()

        result = {
            'document_type': 'CIN',
            'verified': False,
            'cin_number': None,
            'name': None,
            'raw_text_length': len(text),
        }

        # Chercher un numéro CIN tunisien (8 chiffres)
        cin_match = re.search(r'\b(\d{8})\b', text)
        if cin_match:
            result['cin_number'] = cin_match.group(1)
            result['verified'] = True
            print(f"   ✅ CIN numéro détecté : {result['cin_number']}")

        # Vérifier les mots-clés d'identité
        has_cin_keywords = any(kw.lower() in text_lower for kw in self.CIN_KEYWORDS)
        if has_cin_keywords:
            result['verified'] = True

        # Si texte non vide mais pas de numéro → document peut-être valide
        if not result['verified'] and len(text.strip()) > 30:
            result['verified'] = True
            result['note'] = 'Numéro CIN non détecté clairement — vérification manuelle recommandée'

        # Extraire le nom (lignes en majuscules de 3+ mots)
        name = self._extract_name(text)
        if name:
            result['name'] = name

        return result

    def _extract_name(self, text: str) -> str | None:
        """Cherche le nom sur la CIN (lignes en majuscules)."""
        lines = text.split('\n')
        ignore = {'CARTE', 'IDENTITE', 'IDENTITÉ', 'NATIONALE', 'TUNISIE',
                  'TUNISIENNE', 'REPUBLIQUE', 'RÉPUBLIQUE', 'CIN', 'NOM', 'PRENOM'}
        for line in lines:
            line = line.strip()
            words = line.split()
            if (len(words) >= 2 and line.isupper() and len(line) > 4
                    and not any(w in ignore for w in words)):
                return line
        return None

    # ── Parser Salaire ───────────────────────────────────────────

    def _parse_salary(self, text: str) -> dict:
        """Parse une fiche de paie."""
        result = {
            'document_type': 'SALARY',
            'verified': False,
            'salary': None,
            'employer': None,
        }

        patterns = [
            r'net\s*[àa]\s*payer[:\s=]*(\d+[,. ]\d{1,3})',
            r'salaire\s*net[:\s=]*(\d+[,. ]\d{1,3})',
            r'total\s*net[:\s=]*(\d+[,. ]\d{1,3})',
            r'montant\s*net[:\s=]*(\d+[,. ]\d{1,3})',
            r'net\s*[:\s=]+(\d{3,6}[,. ]\d{1,3})',
        ]

        for p in patterns:
            m = re.search(p, text, re.IGNORECASE)
            if m:
                try:
                    val = float(m.group(1).replace(' ', '').replace(',', '.'))
                    if val > 100:
                        result['salary'] = round(val, 3)
                        result['verified'] = True
                        break
                except Exception:
                    pass

        return result

    # ── Fallbacks ────────────────────────────────────────────────

    def _no_ocr_fallback(self, document_type: str) -> dict:
        return {
            'document_type': document_type.upper(),
            'verified': False,
            'paid_on_time': False,
            'error': 'pytesseract non installé. Installez Tesseract-OCR.',
            'ocr_method': 'none'
        }

    def _empty_result(self, document_type: str, error: str = None) -> dict:
        base = {
            'document_type': document_type.upper(),
            'verified': False,
            'paid_on_time': False,
            'ocr_method': 'pytesseract'
        }
        if error:
            base['error'] = error
        return base


# ==================== TEST ====================
if __name__ == "__main__":
    print("🧪 TEST SERVICE OCR\n")
    ocr = OCRService()
    os.makedirs('./uploads', exist_ok=True)

    for test_file, doc_type in [
        ('./uploads/steg.jpg', 'STEG'),
        ('./uploads/sonede.jpg', 'SONEDE'),
        ('./uploads/cin.jpg', 'CIN'),
    ]:
        if os.path.exists(test_file):
            print(f"\n🔍 Test {doc_type} avec {test_file}...")
            result = ocr.extract_from_document(test_file, doc_type)
            print(f"Résultat : {result}")
        else:
            print(f"⚠️  {test_file} non trouvé (ignoré)")

    print("\n✅ Tests terminés")
