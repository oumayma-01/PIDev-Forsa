"""
FICHIER 2 : CRÉER DATASET D'ENTRAÎNEMENT
==========================================
VERSION CORRIGÉE pour Python 3.14
"""

import pandas as pd
import numpy as np
import mysql.connector


class FeatureExtractor:
    """Extrait les features comportementales d'un client"""
    
    def __init__(self, db_config):
        self.conn = mysql.connector.connect(**db_config)
        print("✅ Connecté à MySQL ForsaBD")
    
    def extract_all_features(self, client_id):
        # IMPORTANT : Convertir en int Python normal !
        client_id = int(client_id)
        
        features = {}
        features['f1_salary'] = 0
        features['f2_income_stability'] = self._calc_income_stability(client_id)
        features['f3_savings_rate'] = self._calc_savings_rate(client_id)
        features['f4_account_age'] = self._calc_account_age(client_id)
        features['f5_current_balance'] = self._calc_current_balance(client_id)
        features['f6_activity'] = self._calc_activity(client_id)
        features['f7_avg_income'] = self._calc_avg_income(client_id)
        features['f8_steg_on_time'] = 0
        features['f9_sonede_on_time'] = 0
        features['f10_cin_verified'] = 0
        return features
    
    def _calc_income_stability(self, client_id):
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
        cursor = self.conn.cursor()
        cursor.execute(query, (int(client_id),))
        rows = cursor.fetchall()
        cursor.close()
        
        if len(rows) > 1:
            incomes = [float(r[1]) for r in rows]
            mean = np.mean(incomes)
            std = np.std(incomes)
            if mean > 0:
                return float(std / mean)
        return 999.0
    
    def _calc_savings_rate(self, client_id):
        query = """
        SELECT 
            SUM(CASE WHEN t.type IN ('DEPOSIT', 'TRANSFER_IN') THEN t.amount ELSE 0 END) as income,
            SUM(CASE WHEN t.type IN ('WITHDRAW', 'TRANSFER_OUT') THEN t.amount ELSE 0 END) as spending
        FROM transaction t
        JOIN wallet w ON t.wallet_id = w.id
        WHERE w.owner_id = %s
          AND t.date >= DATE_SUB(NOW(), INTERVAL 6 MONTH)
        """
        cursor = self.conn.cursor()
        cursor.execute(query, (int(client_id),))
        row = cursor.fetchone()
        cursor.close()
        
        income = float(row[0] or 0)
        spending = float(row[1] or 0)
        
        if income > 0:
            return (income - spending) / income
        return -1.0
    
    def _calc_account_age(self, client_id):
        query = "SELECT DATEDIFF(NOW(), created_at) / 30 FROM user WHERE id = %s"
        cursor = self.conn.cursor()
        cursor.execute(query, (int(client_id),))
        row = cursor.fetchone()
        cursor.close()
        
        if row and row[0]:
            return float(row[0])
        return 0.0
    
    def _calc_current_balance(self, client_id):
        query = "SELECT balance FROM wallet WHERE owner_id = %s"
        cursor = self.conn.cursor()
        cursor.execute(query, (int(client_id),))
        row = cursor.fetchone()
        cursor.close()
        
        if row and row[0]:
            return float(row[0])
        return 0.0
    
    def _calc_activity(self, client_id):
        query = """
        SELECT 
            COUNT(*) as total,
            DATEDIFF(MAX(t.date), MIN(t.date)) + 1 as days
        FROM transaction t
        JOIN wallet w ON t.wallet_id = w.id
        WHERE w.owner_id = %s
        """
        cursor = self.conn.cursor()
        cursor.execute(query, (int(client_id),))
        row = cursor.fetchone()
        cursor.close()
        
        if row and row[0] and row[1]:
            transactions = int(row[0])
            days = int(row[1])
            if days > 0:
                return float(transactions / days)
        return 0.0
    
    def _calc_avg_income(self, client_id):
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
        cursor = self.conn.cursor()
        cursor.execute(query, (int(client_id),))
        row = cursor.fetchone()
        cursor.close()
        
        if row and row[0]:
            return float(row[0])
        return 0.0
    
    def close(self):
        self.conn.close()


def create_dataset(db_config, output_file='training_data.csv'):
    extractor = FeatureExtractor(db_config)
    
    # Récupérer TOUS les clients
    cursor = extractor.conn.cursor()
    cursor.execute("SELECT id FROM user")
    clients = cursor.fetchall()
    cursor.close()
    
    print(f"📊 Trouvé {len(clients)} clients\n")
    
    if len(clients) == 0:
        print("❌ AUCUN client ! Ajoute des users dans ta DB !")
        return None
    
    all_features = []
    for row in clients:
        client_id = int(row[0])  # Conversion explicite
        try:
            features = extractor.extract_all_features(client_id)
            features['client_id'] = client_id
            all_features.append(features)
            print(f"  ✅ Client {client_id} OK")
        except Exception as e:
            print(f"  ❌ Client {client_id} : {e}")
    
    if len(all_features) == 0:
        print("\n❌ Aucun client traité avec succès !")
        return None
    
    df = pd.DataFrame(all_features)
    print(f"\n📊 DataFrame créé avec {len(df)} lignes")
    print(f"📊 Colonnes : {df.columns.tolist()}")
    
    # Créer les labels
    def create_label(row):
        score = 0
        if row.get('f3_savings_rate', 0) > 0.2:
            score += 1
        if row.get('f4_account_age', 0) > 3:
            score += 1
        if row.get('f6_activity', 0) > 0.05:
            score += 1
        if row.get('f5_current_balance', 0) > 500:
            score += 1
        if row.get('f2_income_stability', 999) < 0.5:
            score += 1
        
        return 1 if score >= 3 else 0
    
    df['label'] = df.apply(create_label, axis=1)
    
    # Sauvegarder
    df.to_csv(output_file, index=False)
    
    print(f"\n✅ Dataset sauvegardé : {output_file}")
    print(f"   Total : {len(df)}")
    print(f"   Bons (label=1) : {sum(df['label']==1)}")
    print(f"   Mauvais (label=0) : {sum(df['label']==0)}")
    
    extractor.close()
    return df


if __name__ == "__main__":
    db_config = {
        'host': 'localhost',
        'user': 'root',
        'password': '',
        'database': 'ForsaBD'
    }
    create_dataset(db_config)