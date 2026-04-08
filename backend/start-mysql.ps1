# Script de démarrage de l'application FORSA avec MySQL
# Nécessite MySQL en cours d'exécution sur localhost:3306

Write-Host "🚀 Démarrage de l'application FORSA (MySQL)..." -ForegroundColor Green
Write-Host ""

# Vérifier que Maven Wrapper existe
if (-not (Test-Path ".\mvnw.cmd")) {
    Write-Host "❌ Erreur : mvnw.cmd non trouvé !" -ForegroundColor Red
    Write-Host "Assurez-vous d'être dans le dossier racine du projet." -ForegroundColor Yellow
    exit 1
}

Write-Host "🔍 Vérification de MySQL..." -ForegroundColor Yellow

# Tester la connexion MySQL
try {
    $testConnection = Test-NetConnection -ComputerName localhost -Port 3306 -WarningAction SilentlyContinue
    if (-not $testConnection.TcpTestSucceeded) {
        Write-Host "❌ MySQL n'est pas accessible sur localhost:3306" -ForegroundColor Red
        Write-Host ""
        Write-Host "Options :" -ForegroundColor Yellow
        Write-Host "  1. Démarrez MySQL" -ForegroundColor White
        Write-Host "  2. OU utilisez le profil local (H2) : .\start-local.ps1" -ForegroundColor White
        Write-Host ""
        exit 1
    }
    Write-Host "✅ MySQL est accessible" -ForegroundColor Green
} catch {
    Write-Host "⚠️  Impossible de vérifier MySQL, on continue quand même..." -ForegroundColor Yellow
}

Write-Host ""
Write-Host "✅ Profil actif : default (MySQL)" -ForegroundColor Cyan
Write-Host "✅ Base de données : ForsaBD" -ForegroundColor Cyan
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

