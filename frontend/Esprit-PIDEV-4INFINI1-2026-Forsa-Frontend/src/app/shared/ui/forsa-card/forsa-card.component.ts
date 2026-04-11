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
    }
    .shadow-sm {
      box-shadow: 0 1px 3px rgb(0 0 0 / 0.06);
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
