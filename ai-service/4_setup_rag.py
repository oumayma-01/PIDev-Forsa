"""
FICHIER 4 : CONFIGURATION DU SYSTÈME RAG
==========================================
Crée une base de données vectorielle ChromaDB avec :
- Politiques BCT (règles bancaires)
- Cas clients historiques (succès/échecs)

COMMENT ÇA MARCHE ?
1. Sentence-Transformers transforme le texte en vecteurs (384 nombres)
2. ChromaDB stocke ces vecteurs
3. Pour chercher : on transforme la query en vecteur et on cherche les plus proches
"""

import chromadb
from sentence_transformers import SentenceTransformer
import os


def setup_rag():
    print("🔧 Configuration du RAG...\n")
    
    # Créer le dossier ChromaDB
    os.makedirs('./chroma_db', exist_ok=True)
    
    # Initialiser ChromaDB (mode persistant = sauvegarde sur disque)
    client = chromadb.PersistentClient(path='./chroma_db')
    print("✅ ChromaDB initialisé")
    
    # Charger le modèle d'embeddings
    # Ce modèle transforme un texte en vecteur de 384 nombres
    print("⏳ Chargement du modèle d'embeddings...")
    embedding_model = SentenceTransformer('sentence-transformers/all-MiniLM-L6-v2')
    print("✅ Modèle chargé\n")
    
    # Créer 2 collections (comme des tables)
    policies = client.get_or_create_collection("bct_policies")
    cases = client.get_or_create_collection("client_cases")
    
    # ============ POLITIQUES BCT ============
    print("📄 Ajout des politiques BCT...")
    
    bct_policies = [
        {
            "id": "bct_salarie",
            "text": """
Politique BCT pour Salariés avec CDI:
Article 12.5 - Les salariés avec contrat à durée indéterminée bénéficient 
de conditions favorables. Montant maximum = 3x le salaire mensuel pour 
un premier crédit. Après un premier crédit remboursé avec succès, le 
multiplicateur passe à 5x. Durée maximum 36 mois.
""",
            "source": "BCT Circulaire 2019-22",
            "category": "salarie"
        },
        {
            "id": "bct_freelance",
            "text": """
Politique BCT pour Travailleurs Indépendants (Freelance):
Article 45.2 - Les freelances ont des revenus variables nécessitant 
prudence. Montant maximum = 1.5x revenu mensuel moyen pour nouveau client.
Exigence : démontrer 3 mois minimum de revenus supérieurs à 500 TND.
Après premier crédit remboursé : multiplicateur passe à 2.5x.
""",
            "source": "BCT Circulaire 2020-15",
            "category": "freelance"
        },
        {
            "id": "bct_nouveau_client",
            "text": """
Politique BCT pour Nouveaux Clients:
Article 78.1 - Pour un premier crédit, le montant accordé dépend :
- Score < 500 : Refus automatique
- Score 500-650 : Montant = 1x le salaire, durée 6 mois max
- Score 650-750 : Montant = 1.5x le salaire, durée 12 mois max
- Score > 750 : Montant = 2x le salaire, durée 18 mois max
Exigence OCR : CIN vérifiée, fiche de paie authentique.
""",
            "source": "BCT Programme Nouveaux Clients 2023",
            "category": "nouveau"
        },
        {
            "id": "bct_client_fidele",
            "text": """
Politique BCT pour Clients Fidèles (Progressive):
Article 92.3 - Après chaque crédit remboursé à 100% à temps:
- 1er crédit remboursé : Seuil = 2.5x salaire, +100 points
- 2ème crédit remboursé : Seuil = 3x salaire, +80 points
- 3ème crédit remboursé : Seuil = 3.5x salaire, +60 points
- 4ème+ crédit : Seuil = 4.5x salaire (max premium)
En cas de retard : -20 points par retard (<7j), -50 points (>7j).
""",
            "source": "BCT Programme Fidélité 2024",
            "category": "client_fidele"
        },
        {
            "id": "bct_exclusion",
            "text": """
Critères d'Exclusion Automatique:
Article 67.3 - Refus IMMÉDIAT si :
- Score total < 400/1000
- Historique de défaut de paiement (>30 jours)
- Ratio dette/revenu > 60%
- Compte FORSA actif < 3 mois
- Documents CIN/Salaire non vérifiables
- Factures STEG/SONEDE impayées depuis >60 jours
""",
            "source": "BCT Règlement Général",
            "category": "exclusion"
        },
        {
            "id": "bct_recovery",
            "text": """
Amélioration du Score après Difficultés:
Article 85.1 - Un client avec score dégradé peut le remonter par :
- Remboursement intégral du crédit en cours (+100 points)
- Paiements à temps pendant 6 mois (+30 points)
- Épargne constante sur le wallet (+20 points)
- Factures STEG/SONEDE à temps pendant 3 mois (+15 points)
Le score évolue automatiquement sans intervention humaine.
""",
            "source": "BCT Programme Recovery 2024",
            "category": "recovery"
        }
    ]
    
    for policy in bct_policies:
        # Transformer le texte en vecteur
        embedding = embedding_model.encode(policy['text']).tolist()
        
        # Stocker dans ChromaDB
        policies.add(
            ids=[policy['id']],
            embeddings=[embedding],
            documents=[policy['text']],
            metadatas=[{
                "source": policy['source'],
                "category": policy['category']
            }]
        )
        print(f"  ✅ {policy['id']}")
    
    # ============ CAS CLIENTS ============
    print("\n📄 Ajout des cas clients...")
    
    client_cases = [
        {
            "id": "case_success_freelance",
            "text": """
CAS DE SUCCÈS #001 :
Profil: Freelance designer, 28 ans, Tunis
Salaire moyen: 480 TND/mois (variable)
Score initial: 715/1000
Demande: 3000 TND sur 10 mois
Approuvé: 3000 TND
Résultat: Remboursé 100% en 10 mois, 1 retard de 3 jours
Ponctualité finale: 90%
Score final: 750/1000 (+35)
Leçon: Freelances avec bonne ponctualité STEG/SONEDE sont fiables
""",
            "outcome": "success",
            "profile": "freelance"
        },
        {
            "id": "case_success_salarie",
            "text": """
CAS DE SUCCÈS #002 :
Profil: Salarié banque, 35 ans, Sfax
Salaire: 1800 TND/mois (CDI)
Score initial: 820/1000
Demande: 5000 TND sur 12 mois
Approuvé: 5000 TND (plafond premium)
Résultat: Remboursé 100% à temps, 0 retard
Score final: 900/1000 (+80)
Leçon: Salariés CDI avec haute ponctualité = clients premium
"""
,
            "outcome": "success",
            "profile": "salarie_cdi"
        },
        {
            "id": "case_default",
            "text": """
CAS D'ÉCHEC #003 :
Profil: Jeune, 22 ans, compte récent (3 mois)
Salaire: 380 TND (irrégulier)
Score initial: 480/1000
Demande: 2000 TND sur 8 mois
Approuvé: 380 TND (1x salaire, limité)
Résultat: Défaut au 3ème mois, 45 jours de retard
Score final: 280/1000 (-200)
Leçon: Compte récent + score <500 = risque élevé
""",
            "outcome": "default",
            "profile": "jeune_risque"
        },
        {
            "id": "case_recovery",
            "text": """
CAS DE RECOVERY #004 :
Profil: Commerçant, 40 ans
Salaire: 700 TND (activité)
Score initial après défaut: 350/1000
Action: Remboursement crédit précédent après négociation
Paiements STEG/SONEDE à temps pendant 6 mois
Score actuel: 580/1000 (+230)
Leçon: Recovery possible avec régularisation et ponctualité
""",
            "outcome": "recovery",
            "profile": "recovery"
        }
    ]
    
    for case in client_cases:
        embedding = embedding_model.encode(case['text']).tolist()
        cases.add(
            ids=[case['id']],
            embeddings=[embedding],
            documents=[case['text']],
            metadatas=[{
                "outcome": case['outcome'],
                "profile": case['profile']
            }]
        )
        print(f"  ✅ {case['id']}")
    
    print("\n✅ RAG configuré avec succès !")
    print(f"   - Politiques BCT : {policies.count()}")
    print(f"   - Cas clients : {cases.count()}")
    
    return client


def test_rag_search():
    """Test de la recherche RAG"""
    print("\n🧪 TEST DE RECHERCHE RAG...\n")
    
    client = chromadb.PersistentClient(path='./chroma_db')
    embedding_model = SentenceTransformer('sentence-transformers/all-MiniLM-L6-v2')
    
    policies = client.get_collection("bct_policies")
    
    # Query test
    query = "Client freelance nouveau, demande un premier crédit"
    query_embedding = embedding_model.encode(query).tolist()
    
    results = policies.query(
        query_embeddings=[query_embedding],
        n_results=2
    )
    
    print(f"🔍 Query : '{query}'\n")
    print("📄 Documents trouvés :\n")
    
    for i, doc in enumerate(results['documents'][0]):
        print(f"--- Document {i+1} ---")
        print(doc[:200] + "...")
        print(f"Source : {results['metadatas'][0][i]['source']}")
        print()


if __name__ == "__main__":
    setup_rag()
    test_rag_search()