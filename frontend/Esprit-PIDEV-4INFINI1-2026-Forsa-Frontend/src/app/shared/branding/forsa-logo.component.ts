import { ChangeDetectionStrategy, Component, Input } from '@angular/core';

/** Light / dark logos from `/public` (switched with `html.dark`). */
@Component({
  selector: 'app-forsa-logo',
  standalone: true,
  changeDetection: ChangeDetectionStrategy.OnPush,
  template: `
    <img
      src="forsa-logo-light.png"
      [attr.alt]="alt"
      [attr.width]="width ?? null"
      [attr.height]="height"
      class="forsa-logo-img forsa-logo--light"
      loading="eager"
      decoding="async"
    />
    <img
      src="forsa-logo-dark.png"
      [attr.alt]="alt"
      [attr.width]="width ?? null"
      [attr.height]="height"
      class="forsa-logo-img forsa-logo--dark"
      loading="eager"
      decoding="async"
    />
  `,
  styles: `
    :host {
      display: inline-flex;
      align-items: center;
      line-height: 0;
      position: relative;
    }
    .forsa-logo-img {
      width: auto;
      max-width: 100%;
      object-fit: contain;
    }
  `,
})
export class ForsaLogoComponent {
  @Input() height = 40;
  /** If set, fixes width; otherwise width follows aspect ratio. */
  @Input() width: number | null = null;
  @Input() alt = 'Forsa';
}
