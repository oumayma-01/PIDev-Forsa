import { Component, DestroyRef, inject, OnInit } from '@angular/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { SwUpdate } from '@angular/service-worker';
import { fromEvent } from 'rxjs';
import { debounceTime, filter } from 'rxjs/operators';

@Component({
  selector: 'app-pwa-update-banner',
  standalone: true,
  template: `
    @if (visible) {
      <div class="pwa-banner" role="status" aria-live="polite">
        <span class="pwa-banner__text">A new version of Forsa is ready.</span>
        <button type="button" class="pwa-banner__btn" (click)="activate()">Refresh</button>
        <button type="button" class="pwa-banner__dismiss" (click)="dismiss()" aria-label="Dismiss">×</button>
      </div>
    }
  `,
  styles: `
    .pwa-banner {
      position: fixed;
      bottom: 0;
      left: 0;
      right: 0;
      z-index: 100000;
      display: flex;
      align-items: center;
      justify-content: center;
      gap: 0.75rem;
      flex-wrap: wrap;
      padding: 0.75rem 1rem;
      padding-bottom: max(0.75rem, env(safe-area-inset-bottom));
      background: #0f172a;
      color: #f8fafc;
      font-size: 0.875rem;
      box-shadow: 0 -8px 24px rgba(0, 0, 0, 0.2);
    }
    .pwa-banner__text {
      flex: 1 1 auto;
      text-align: center;
      min-width: 0;
    }
    .pwa-banner__btn {
      border: none;
      border-radius: 0.5rem;
      padding: 0.45rem 1rem;
      font-weight: 600;
      cursor: pointer;
      background: #2dd4bf;
      color: #0f172a;
    }
    .pwa-banner__dismiss {
      border: none;
      background: transparent;
      color: #94a3b8;
      font-size: 1.35rem;
      line-height: 1;
      cursor: pointer;
      padding: 0.25rem 0.5rem;
    }
  `,
})
export class PwaUpdateBannerComponent implements OnInit {
  private readonly swUpdate = inject(SwUpdate);
  private readonly destroyRef = inject(DestroyRef);

  protected visible = false;

  ngOnInit(): void {
    if (!this.swUpdate.isEnabled) {
      return;
    }
    this.swUpdate.versionUpdates
      .pipe(
        filter((e) => e.type === 'VERSION_READY'),
        takeUntilDestroyed(this.destroyRef),
      )
      .subscribe(() => {
        this.visible = true;
      });

    fromEvent(window, 'focus')
      .pipe(debounceTime(2000), takeUntilDestroyed(this.destroyRef))
      .subscribe(() => {
        if (this.swUpdate.isEnabled) {
          void this.swUpdate.checkForUpdate();
        }
      });

    if (this.swUpdate.isEnabled) {
      void this.swUpdate.checkForUpdate();
    }
  }

  dismiss(): void {
    this.visible = false;
  }

  activate(): void {
    this.swUpdate
      .activateUpdate()
      .then(() => {
        document.location.reload();
      })
      .catch(() => {
        document.location.reload();
      });
  }
}
