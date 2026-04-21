"""
FICHIER 3 : ENTRAÎNER XGBOOST
===============================
Entraîne le modèle de Machine Learning.
Sauvegarde le modèle dans xgboost_model.pkl
"""

import pandas as pd
import numpy as np
import xgboost as xgb
from sklearn.model_selection import train_test_split
from sklearn.preprocessing import StandardScaler
from sklearn.metrics import roc_auc_score, classification_report
import joblib


def train_model():
    print("📊 Chargement des données...")
    df = pd.read_csv('training_data.csv')
    
    feature_cols = ['f1_salary', 'f2_income_stability', 'f3_savings_rate',
                    'f4_account_age', 'f5_current_balance', 'f6_activity',
                    'f7_avg_income', 'f8_steg_on_time', 'f9_sonede_on_time',
                    'f10_cin_verified']
    
    X = df[feature_cols].replace([np.inf, -np.inf], np.nan).fillna(0)
    y = df['label']
    
    # Si pas assez de données, on duplique
    if len(X) < 20:
        print("⚠️ Pas assez de données, duplication...")
        X = pd.concat([X] * 10, ignore_index=True)
        y = pd.concat([y] * 10, ignore_index=True)
    
    # Split train/test
    X_train, X_test, y_train, y_test = train_test_split(
        X, y, test_size=0.2, random_state=42, stratify=y
    )
    
    # Normalisation
    scaler = StandardScaler()
    X_train_scaled = scaler.fit_transform(X_train)
    X_test_scaled = scaler.transform(X_test)
    
    # Entraîner XGBoost
    print("🤖 Entraînement XGBoost...")
    model = xgb.XGBClassifier(
        n_estimators=100,
        max_depth=4,
        learning_rate=0.1,
        random_state=42
    )
    model.fit(X_train_scaled, y_train)
    
    # Évaluation
    y_pred_proba = model.predict_proba(X_test_scaled)[:, 1]
    auc = roc_auc_score(y_test, y_pred_proba)
    
    print(f"\n🎯 AUC-ROC : {auc:.4f}")
    
    # Sauvegarder
    joblib.dump(model, 'xgboost_model.pkl')
    joblib.dump(scaler, 'scaler.pkl')
    print("\n✅ Modèle sauvegardé : xgboost_model.pkl")


if __name__ == "__main__":
    train_model()