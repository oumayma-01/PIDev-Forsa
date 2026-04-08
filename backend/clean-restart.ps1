# Clean Restart Script for PIDev-Forsa Application
# This script stops any existing Java processes and starts the application cleanly

Write-Host "========================================" -ForegroundColor Cyan
Write-Host "PIDev-Forsa Application Clean Restart" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""

# Step 1: Stop any existing Java processes
Write-Host "Step 1: Stopping any existing Java processes..." -ForegroundColor Yellow
Get-Process java -ErrorAction SilentlyContinue | ForEach-Object {
    Write-Host "  Stopping PID $($_.Id)" -ForegroundColor Yellow
    Stop-Process -Id $_.Id -Force -ErrorAction SilentlyContinue
}

Write-Host "Step 1: ✅ Complete" -ForegroundColor Green
Start-Sleep -Seconds 2

# Step 2: Verify port 8080 is free
Write-Host ""
Write-Host "Step 2: Verifying port 8080 is free..." -ForegroundColor Yellow
$portInUse = netstat -ano 2>$null | Select-String ":8080" | Measure-Object | Select-Object -ExpandProperty Count
if ($portInUse -gt 0) {
    Write-Host "  ⚠️ Warning: Port 8080 is still in use!" -ForegroundColor Red
    Write-Host "  Run this command manually to find the process:" -ForegroundColor Yellow
    Write-Host "  netstat -ano | Select-String ':8080'" -ForegroundColor Yellow
    Exit 1
} else {
    Write-Host "  ✅ Port 8080 is free" -ForegroundColor Green
}

# Step 3: Clean build
Write-Host ""
Write-Host "Step 3: Cleaning project..." -ForegroundColor Yellow
cd "C:\Users\ASUS\Desktop\PIDev-Forsa"
.\mvnw.cmd clean -q
Write-Host "Step 3: ✅ Complete" -ForegroundColor Green

# Step 4: Compile
Write-Host ""
Write-Host "Step 4: Compiling project..." -ForegroundColor Yellow
.\mvnw.cmd compile -q -DskipTests
Write-Host "Step 4: ✅ Complete" -ForegroundColor Green

# Step 5: Start application
Write-Host ""
Write-Host "Step 5: Starting Spring Boot application..." -ForegroundColor Cyan
Write-Host ""
.\mvnw.cmd spring-boot:run

# If we get here, application was stopped
Write-Host ""
Write-Host "Application stopped. To restart, run this script again." -ForegroundColor Yellow

