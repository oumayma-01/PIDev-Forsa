import { ChangeDetectionStrategy, Component, Input } from '@angular/core';
import type { ForsaIconName } from './forsa-icon.types';

@Component({
  selector: 'app-forsa-icon',
  standalone: true,
  templateUrl: './forsa-icon.component.html',
  changeDetection: ChangeDetectionStrategy.OnPush,
  styles: `
    :host {
      display: inline-flex;
      align-items: center;
      justify-content: center;
      color: inherit;
    }
    svg {
      flex-shrink: 0;
    }
  `,
})
export class ForsaIconComponent {
  @Input({ required: true }) name!: ForsaIconName;
  @Input() size = 20;
}
