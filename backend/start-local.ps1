# Script de démarrage de l'application FORSA
# Utilise H2 en mémoire (pas besoin de MySQL)

Write-Host "🚀 Démarrage de l'application FORSA..." -ForegroundColor Green
Write-Host ""

# Vérifier que Maven Wrapper existe
if (-not (Test-Path ".\mvnw.cmd")) {
    Write-Host "❌ Erreur : mvnw.cmd non trouvé !" -ForegroundColor Red
    Write-Host "Assurez-vous d'être dans le dossier racine du projet." -ForegroundColor Yellow
    exit 1
}

# Définir le profil local (H2)
$env:SPRING_PROFILES_ACTIVE = 'local'

Write-Host "✅ Profil actif : local (H2 en mémoire)" -ForegroundColor Cyan
Write-Host "✅ Port : 8089" -ForegroundColor Cyan
Write-Host "✅ Context path : /forsaPidev" -ForegroundColor Cyan
Write-Host ""

Write-Host "📦 Compilation du projet..." -ForegroundColor Yellow
./mvnw.cmd clean compile -DskipTests -q

if ($LASTEXITCODE -ne 0) {
    Write-Host "❌ Erreur de compilation !" -ForegroundColor Red
    exit 1
}

Write-Host "✅ Compilation réussie !" -ForegroundColor Green
Write-Host ""

Write-Host "🏃 Lancement de l'application..." -ForegroundColor Yellow
Write-Host "📍 URL : http://localhost:8089/forsaPidev" -ForegroundColor Cyan
Write-Host "📍 Swagger : http://localhost:8089/forsaPidev/swagger-ui.html" -ForegroundColor Cyan
Write-Host ""
Write-Host "⏳ Appuyez sur Ctrl+C pour arrêter l'application" -ForegroundColor Magenta
Write-Host "================================================" -ForegroundColor Gray
Write-Host ""

./mvnw.cmd spring-boot:run

