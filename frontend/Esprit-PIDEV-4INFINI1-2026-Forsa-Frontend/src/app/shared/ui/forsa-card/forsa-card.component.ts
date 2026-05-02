import { Component, Input } from '@angular/core';

@Component({
  selector: 'app-forsa-card',
  standalone: true,
  template: ` <div [class]="hostClass"><ng-content /></div> `,
  styles: `
    :host {
      display: block;
    }
    div {
      background: var(--color-card);
      color: var(--color-card-foreground);
      border-radius: var(--radius-xl);
      border: 1px solid color-mix(in oklab, var(--color-border) 45%, transparent);
      transition:
        transform 0.3s cubic-bezier(0.4, 0, 0.2, 1),
        box-shadow 0.3s cubic-bezier(0.4, 0, 0.2, 1),
        border-color 0.3s ease;
    }
    :host-context(.dark) div {
      background: var(--glass-bg, rgba(16, 28, 50, 0.85));
      border-color: var(--glass-border, rgba(60, 100, 160, 0.15));
      -webkit-backdrop-filter: blur(12px);
      backdrop-filter: blur(12px);
    }
    .shadow-sm {
      box-shadow: 0 1px 3px rgb(0 0 0 / 0.06), 0 1px 2px rgb(0 0 0 / 0.04);
    }
    :host-context(.dark) .shadow-sm {
      box-shadow: 0 2px 8px rgba(0, 0, 0, 0.2);
    }
    .no-border {
      border: none;
    }
    .overflow-hidden {
      overflow: hidden;
    }
  `,
})
export class ForsaCardComponent {
  @Input() shadow: 'none' | 'sm' = 'sm';
  @Input() noBorder = false;
  @Input() overflowHidden = false;

  get hostClass(): string {
    return [
      this.shadow === 'sm' ? 'shadow-sm' : '',
      this.noBorder ? 'no-border' : '',
      this.overflowHidden ? 'overflow-hidden' : '',
    ]
      .filter(Boolean)
      .join(' ');
  }
}
