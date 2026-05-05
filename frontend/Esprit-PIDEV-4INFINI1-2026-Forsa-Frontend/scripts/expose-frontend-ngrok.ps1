<#!
  Frontend Angular (build statique) + ngrok.

  Si vous voyez ERR_NGROK_334 ("endpoint is already online") :
    - Vous avez lance deux fois le meme domaine ngrok, OU un tunnel est encore actif ailleurs.
    - Arretez l'ancien ngrok (Ctrl+C) ou fermez la session dans le dashboard ngrok.
    - OU utilisez UNE seule commande pour API + front : a la racine du repo :
        ngrok start --config ngrok-forsa.yml --all
      (fichier ngrok-forsa.yml : tunnels 8089 + 4173, deux URLs differentes)

  Etapes manuelles (deux terminaux ngrok separes) :
    1. apiBaseUrl dans environment.mobile.ts = URL HTTPS du tunnel BACKEND
    2. npm run build:mobile
    3. npm run serve:dist   -> http://127.0.0.1:4173
    4. Terminal A : ngrok http 8089   (sans reutiliser un --domain deja pris)
    5. Terminal B : ngrok http 4173
    6. Ouvrir sur le tel l'URL du tunnel FRONT
#>
Write-Host ""
Write-Host "1) npm run build:mobile" -ForegroundColor Cyan
Write-Host "2) npm run serve:dist   (garde ce terminal ouvert)" -ForegroundColor Cyan
Write-Host "3) ngrok http 4173      (dans un AUTRE terminal)" -ForegroundColor Cyan
Write-Host ""
Write-Host "URL API dans environment.mobile.ts doit pointer vers l'autre tunnel (backend), ex.:" -ForegroundColor Yellow
Write-Host "  https://BACKEND_SUBDOMAIN.ngrok-free.app/forsaPidev/api" -ForegroundColor Gray
Write-Host ""
