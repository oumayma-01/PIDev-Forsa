import { Component, Input } from '@angular/core';

@Component({
  selector: 'app-forsa-badge',
  standalone: true,
  template: ` <span [class]="classes"><ng-content /></span> `,
  styles: `
    :host {
      display: inline-flex;
    }
    span {
      display: inline-flex;
      align-items: center;
      gap: 0.25rem;
      border-radius: 9999px;
      font-size: 0.75rem;
      font-weight: 600;
      padding: 0.125rem 0.65rem;
      line-height: 1.25;
    }
    .tone-default {
      background: color-mix(in oklab, var(--color-primary) 12%, transparent);
      color: var(--color-primary);
    }
    .tone-secondary {
      background: var(--color-secondary);
      color: var(--color-secondary-foreground);
    }
    .tone-success {
      background: var(--color-emerald-100);
      color: var(--color-emerald-700);
    }
    .tone-warning {
      background: var(--color-amber-100);
      color: var(--color-amber-700);
    }
    .tone-danger {
      background: color-mix(in oklab, var(--color-destructive) 14%, transparent);
      color: var(--color-destructive);
    }
    .tone-info {
      background: var(--color-blue-100);
      color: var(--color-blue-700);
    }
    .tone-muted {
      background: var(--color-muted);
      color: var(--color-muted-foreground);
    }
    .size-xs {
      font-size: 0.625rem;
      padding: 0.1rem 0.45rem;
    }
  `,
})
export class ForsaBadgeComponent {
  @Input() tone: 'default' | 'secondary' | 'success' | 'warning' | 'danger' | 'info' | 'muted' =
    'default';
  @Input() size: 'sm' | 'xs' = 'sm';
  @Input() extraClass = '';

  get classes(): string {
    return [`tone-${this.tone}`, this.size === 'xs' ? 'size-xs' : '', this.extraClass].filter(Boolean).join(' ');
  }
}
