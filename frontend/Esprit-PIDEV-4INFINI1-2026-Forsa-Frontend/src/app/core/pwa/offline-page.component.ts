import { Component } from '@angular/core';
import { RouterLink } from '@angular/router';

@Component({
  selector: 'app-offline-page',
  standalone: true,
  imports: [RouterLink],
  template: `
    <main class="offline">
      <h1>You are offline</h1>
      <p>Forsa needs a connection for sign-in and live data. Cached pages may still open.</p>
      <div class="actions">
        <button type="button" class="btn" (click)="retry()">Try again</button>
        <a routerLink="/" class="btn btn--ghost">Home</a>
      </div>
    </main>
  `,
  styles: `
    .offline {
      min-height: 100vh;
      display: flex;
      flex-direction: column;
      align-items: center;
      justify-content: center;
      padding: 2rem;
      text-align: center;
      font-family: system-ui, sans-serif;
      background: linear-gradient(165deg, #0f172a 0%, #134e4a 100%);
      color: #f8fafc;
    }
    h1 {
      margin: 0 0 0.75rem;
      font-size: 1.5rem;
    }
    p {
      margin: 0 0 1.5rem;
      max-width: 26rem;
      color: #cbd5e1;
      line-height: 1.5;
    }
    .actions {
      display: flex;
      flex-wrap: wrap;
      gap: 0.75rem;
      justify-content: center;
    }
    .btn {
      display: inline-flex;
      align-items: center;
      justify-content: center;
      padding: 0.65rem 1.25rem;
      border-radius: 0.65rem;
      font-weight: 600;
      font-size: 0.9rem;
      border: none;
      cursor: pointer;
      text-decoration: none;
      background: #2dd4bf;
      color: #0f172a;
    }
    .btn--ghost {
      background: transparent;
      color: #e2e8f0;
      border: 1px solid rgba(255, 255, 255, 0.25);
    }
  `,
})
export class OfflinePageComponent {
  retry(): void {
    window.location.reload();
  }
}
