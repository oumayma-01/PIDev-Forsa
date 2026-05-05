<#!
  Expose le backend Spring (port 8089) sur Internet via ngrok.

  Prérequis (une fois) :
    1. Compte sur https://dashboard.ngrok.com/
    2. ngrok config add-authtoken <VOTRE_TOKEN>

  Ensuite :
    1. Démarrez le backend avec le profil ngrok :
         cd backend
         mvn spring-boot:run "-Dspring-boot.run.profiles=ngrok"
    2. Lancez ce script (autre terminal) :
         powershell -ExecutionPolicy Bypass -File scripts/start-ngrok-tunnel.ps1

  Copiez l'URL HTTPS affichée (ex. https://abc123.ngrok-free.app).
  Dans le frontend (environment.mobile.ts), mettez :
    apiBaseUrl: 'https://abc123.ngrok-free.app/forsaPidev/api'
#>
$ErrorActionPreference = "Stop"
$port = 8089
if (-not (Get-Command ngrok -ErrorAction SilentlyContinue)) {
  Write-Error "ngrok n'est pas dans le PATH. Installez-le depuis https://ngrok.com/download"
}
Write-Host "Tunnel ngrok -> http://127.0.0.1:$port (context-path Spring: /forsaPidev)" -ForegroundColor Cyan
Write-Host "Pensez a demarrer Spring avec --spring.profiles.active=ngrok" -ForegroundColor Yellow
Write-Host "Si ERR_NGROK_334 : arretez l'autre ngrok, ou depuis la racine du repo : ngrok start --config ngrok-forsa.yml --all" -ForegroundColor DarkYellow
& ngrok http $port
