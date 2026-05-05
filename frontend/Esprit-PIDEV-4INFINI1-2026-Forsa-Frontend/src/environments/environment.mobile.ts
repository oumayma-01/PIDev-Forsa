/**
 * Build for tests depuis un téléphone (même Wi‑Fi ou tunnel HTTPS).
 *
 * 1. Remplacez `API_HOST` par l’URL réelle de votre API (sans slash final).
 *    Exemples :
 *    - Même Wi‑Fi : `http://192.168.1.50:8089/forsaPidev/api`
 *    - Tunnel (recommandé pour PWA) : `https://xxxx.ngrok-free.app/forsaPidev/api`
 *
 * 2. Backend + ngrok : `mvn spring-boot:run "-Dspring-boot.run.profiles=ngrok"` depuis `backend/`
 *    puis `powershell -File backend/scripts/start-ngrok-tunnel.ps1` (voir commentaires du script).
 *
 * 3. Build + serve local puis ngrok FRONT :
 *      npm run build:mobile
 *      npm run serve:dist          -> http://127.0.0.1:4173 (SPA)
 *      ngrok http 4173            -> ouvrir l'URL https sur le telephone
 *    Voir aussi scripts/expose-frontend-ngrok.ps1 (rappel des commandes).
 *
 *    Sur HTTP + IP seule, le service worker peut etre refuse ; HTTPS ngrok convient pour la PWA.
 *
 * Passkeys : ouvrir l'app exactement sur le meme host que `app.frontend.base-url` cote Spring
 * (ex. https://bigotedly-youthful-dino.ngrok-free.dev avec le profil `ngrok` sur le backend).
 */
export const environment = {
  production: true,
  apiBaseUrl: 'https://forsa-backend.loca.lt/forsaPidev/api',
  defaultClientRoleId: 2,
  defaultAgentRoleId: 3,
};
