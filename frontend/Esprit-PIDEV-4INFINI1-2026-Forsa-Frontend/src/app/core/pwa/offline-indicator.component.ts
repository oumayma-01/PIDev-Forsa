import { Component, DestroyRef, inject, OnInit } from '@angular/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { fromEvent, merge } from 'rxjs';
import { map, startWith } from 'rxjs/operators';

@Component({
  selector: 'app-offline-indicator',
  standalone: true,
  template: `
    @if (!online) {
      <div class="offline-strip" role="status" aria-live="polite">
        No network connection — some features are unavailable until you are back online.
      </div>
    }
  `,
  styles: `
    .offline-strip {
      position: fixed;
      top: 0;
      left: 0;
      right: 0;
      z-index: 99999;
      padding: 0.45rem 0.75rem;
      padding-top: max(0.45rem, env(safe-area-inset-top));
      text-align: center;
      font-size: 0.8rem;
      font-weight: 600;
      background: #b45309;
      color: #fffbeb;
    }
  `,
})
export class OfflineIndicatorComponent implements OnInit {
  private readonly destroyRef = inject(DestroyRef);

  protected online = true;

  ngOnInit(): void {
    merge(
      fromEvent(window, 'online').pipe(map(() => true)),
      fromEvent(window, 'offline').pipe(map(() => false)),
    )
      .pipe(startWith(navigator.onLine), takeUntilDestroyed(this.destroyRef))
      .subscribe((v) => {
        this.online = v;
      });
  }
}
