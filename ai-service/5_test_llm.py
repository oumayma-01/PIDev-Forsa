"""
FICHIER 5 : TEST RAPIDE DE MISTRAL
"""

import requests

print("🧪 TEST DE MISTRAL VIA OLLAMA\n")

url = "http://localhost:11434/api/generate"
payload = {
    "model": "mistral",
    "prompt": "Dis bonjour en français en une phrase courte.",
    "stream": False
}

try:
    print("⏳ Envoi de la requête à Mistral...")
    response = requests.post(url, json=payload, timeout=60)
    
    if response.status_code == 200:
        result = response.json()
        print("\n✅ MISTRAL RÉPOND :")
        print("─" * 50)
        print(result['response'])
        print("─" * 50)
    else:
        print(f"❌ Erreur {response.status_code}")
        
except Exception as e:
    print(f"❌ Erreur : {e}")
    print("\n💡 Vérifie que Ollama tourne (icône dans la barre de tâches)")