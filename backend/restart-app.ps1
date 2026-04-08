# Restart Application Script

# Stop any existing Java processes running the application
Write-Host "Stopping any existing application processes..." -ForegroundColor Yellow
Stop-Process -Name java -ErrorAction SilentlyContinue

Start-Sleep -Seconds 2

# Clean and compile
Write-Host "Cleaning project..." -ForegroundColor Cyan
cd "C:\Users\ASUS\Desktop\PIDev-Forsa"
.\mvnw.cmd clean -q

Write-Host "Compiling project..." -ForegroundColor Cyan
.\mvnw.cmd compile -q -DskipTests

# Start the application
Write-Host "Starting application..." -ForegroundColor Green
.\mvnw.cmd spring-boot:run

