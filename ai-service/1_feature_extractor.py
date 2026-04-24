"""
FICHIER 1 : EXTRACTION DES FEATURES
====================================
Ce fichier extrait les features comportementales du client 
depuis ta base de données MySQL.

ADAPTÉ À TA DB :
- Table user (id, created_at, username, ...)
- Table wallet (id, balance, owner_id)
- Table transaction (id, amount, date, type, wallet_id)
"""

import pandas as pd
import numpy as np
import mysql.connector


class FeatureExtractor:
    """Extrait les features comportementales d'un client"""
    
    def __init__(self, db_config):
        """
        db_config = {
            'host': 'localhost',
            'user': 'root',
            'password': '',  # XAMPP par défaut
            'database': 'ForsaBD'
        }
        """
        self.conn = mysql.connector.connect(**db_config)
        print("✅ Connecté à MySQL ForsaBD")
    
    def extract_all_features(self, client_id):
        """
        Extrait les 7 features comportementales + 3 features documents
        (les documents seront ajoutés après l'OCR)
        """
        features = {}
        
        # === FEATURES COMPORTEMENTALES (depuis ta DB) ===
        
        # Feature 1 : Salaire vérifié (depuis OCR, sera ajouté plus tard)
        features['f1_salary'] = 0  # Placeholder
        
        # Feature 2 : Stabilité du revenu (coefficient de variation)
        features['f2_income_stability'] = self._calc_income_stability(client_id)
        
        # Feature 3 : Taux d'épargne
        features['f3_savings_rate'] = self._calc_savings_rate(client_id)
        
        # Feature 4 : Ancienneté du compte (mois)
        features['f4_account_age'] = self._calc_account_age(client_id)
        
        # Feature 5 : Balance actuelle (liquidité)
        features['f5_current_balance'] = self._calc_current_balance(client_id)
        
        # Feature 6 : Activité (vélocité)
        features['f6_activity'] = self._calc_activity(client_id)
        
        # Feature 7 : Revenu moyen mensuel
        features['f7_avg_income'] = self._calc_avg_income(client_id)
        
        # === FEATURES DOCUMENTS (seront ajoutées par l'OCR) ===
        features['f8_steg_on_time'] = 0    # Placeholder
        features['f9_sonede_on_time'] = 0  # Placeholder
        features['f10_cin_verified'] = 0   # Placeholder
        
        return features
    
    def _calc_income_stability(self, client_id):
        """
        Feature 2 : Stabilité du revenu
        Formule : écart-type / moyenne (coefficient de variation)
        - < 0.2 : Très stable (salarié)
        - > 0.5 : Instable (freelance)
        """
        query = """
        SELECT 
            DATE_FORMAT(t.date, '%Y-%m') as month,
            SUM(t.amount) as monthly_income
        FROM transaction t
        JOIN wallet w ON t.wallet_id = w.id
        WHERE w.owner_id = %s 
          AND t.type IN ('DEPOSIT', 'TRANSFER_IN')
          AND t.date >= DATE_SUB(NOW(), INTERVAL 6 MONTH)
        GROUP BY month
        """
        df = pd.read_sql(query, self.conn, params=(client_id,))
        
        if len(df) > 1:
            incomes = df['monthly_income'].values
            mean = np.mean(incomes)
            std = np.std(incomes)
            if mean > 0:
                return float(std / mean)
        return 999.0  # Très instable par défaut
    
    def _calc_savings_rate(self, client_id):
        """
        Feature 3 : Taux d'épargne
        Formule : (Revenus - Dépenses) / Revenus
        """
        query = """
        SELECT 
            SUM(CASE WHEN t.type IN ('DEPOSIT', 'TRANSFER_IN') THEN t.amount ELSE 0 END) as income,
            SUM(CASE WHEN t.type IN ('WITHDRAW', 'TRANSFER_OUT') THEN t.amount ELSE 0 END) as spending
        FROM transaction t
        JOIN wallet w ON t.wallet_id = w.id
        WHERE w.owner_id = %s
          AND t.date >= DATE_SUB(NOW(), INTERVAL 6 MONTH)
        """
        result = pd.read_sql(query, self.conn, params=(client_id,))
        income = float(result['income'][0] or 0)
        spending = float(result['spending'][0] or 0)
        
        if income > 0:
            return (income - spending) / income
        return -1.0
    
    def _calc_account_age(self, client_id):
        """
        Feature 4 : Ancienneté du compte en mois
        """
        query = """
        SELECT DATEDIFF(NOW(), created_at) / 30 as age_months
        FROM user WHERE id = %s
        """
        result = pd.read_sql(query, self.conn, params=(client_id,))
        return float(result['age_months'][0] or 0)
    
    def _calc_current_balance(self, client_id):
        """
        Feature 5 : Balance actuelle du wallet
        """
        query = """
        SELECT balance FROM wallet WHERE owner_id = %s
        """
        result = pd.read_sql(query, self.conn, params=(client_id,))
        
        if len(result) > 0:
            return float(result['balance'][0] or 0)
        return 0.0
    
    def _calc_activity(self, client_id):
        """
        Feature 6 : Activité du compte
        Formule : nombre de transactions / jours actifs
        """
        query = """
        SELECT 
            COUNT(*) as total_transactions,
            DATEDIFF(MAX(t.date), MIN(t.date)) + 1 as active_days
        FROM transaction t
        JOIN wallet w ON t.wallet_id = w.id
        WHERE w.owner_id = %s
        """
        result = pd.read_sql(query, self.conn, params=(client_id,))
        
        transactions = result['total_transactions'][0] or 0
        days = result['active_days'][0] or 1
        
        if days > 0:
            return float(transactions / days)
        return 0.0
    
    def _calc_avg_income(self, client_id):
        """
        Feature 7 : Revenu moyen mensuel
        """
        query = """
        SELECT AVG(monthly_income) as avg_income
        FROM (
            SELECT DATE_FORMAT(t.date, '%Y-%m') as month,
                   SUM(t.amount) as monthly_income
            FROM transaction t
            JOIN wallet w ON t.wallet_id = w.id
            WHERE w.owner_id = %s 
              AND t.type IN ('DEPOSIT', 'TRANSFER_IN')
              AND t.date >= DATE_SUB(NOW(), INTERVAL 6 MONTH)
            GROUP BY month
        ) temp
        """
        result = pd.read_sql(query, self.conn, params=(client_id,))
        return float(result['avg_income'][0] or 0)
    
    def close(self):
        self.conn.close()


# ============ TEST ============
if __name__ == "__main__":
    db_config = {
        'host': 'localhost',
        'user': 'root',
        'password': '',
        'database': 'ForsaBD'
    }
    
    extractor = FeatureExtractor(db_config)
    
    # Teste avec un client existant
    client_id = 1  # Change selon ta DB
    
    print(f"\n📊 Calcul des features pour le client {client_id}...")
    features = extractor.extract_all_features(client_id)
    
    print("\n✅ Features calculées :")
    for name, value in features.items():
        print(f"  {name}: {value:.3f}")
    
    extractor.close()