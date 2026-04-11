import { ChangeDetectionStrategy, Component, Input } from '@angular/core';

@Component({
  selector: 'app-mini-area-chart',
  standalone: true,
  changeDetection: ChangeDetectionStrategy.OnPush,
  template: `
    <svg [attr.viewBox]="'0 0 ' + w + ' ' + h" preserveAspectRatio="none" class="chart">
      <defs>
        <linearGradient [attr.id]="gradId" x1="0" y1="0" x2="0" y2="1">
          <stop offset="0%" [attr.stop-color]="color" stop-opacity="0.35" />
          <stop offset="100%" [attr.stop-color]="color" stop-opacity="0" />
        </linearGradient>
      </defs>
      <polygon [attr.points]="fillPoints" [attr.fill]="'url(#' + gradId + ')'" />
      <polyline [attr.points]="linePoints" fill="none" [attr.stroke]="color" stroke-width="3" stroke-linejoin="round" stroke-linecap="round" />
    </svg>
  `,
  styles: `
    .chart {
      width: 100%;
      height: 100%;
      display: block;
    }
  `,
})
export class MiniAreaChartComponent {
  @Input() values: number[] = [0];
  @Input() w = 400;
  @Input() h = 200;
  @Input() color = 'var(--color-primary)';

  readonly gradId = 'area-' + Math.random().toString(36).slice(2, 9);

  private get max(): number {
    return Math.max(...this.values, 1);
  }

  get linePoints(): string {
    const n = this.values.length;
    return this.values
      .map((v, i) => {
        const x = (i / Math.max(n - 1, 1)) * this.w;
        const y = this.h - (v / this.max) * (this.h - 24) - 12;
        return `${x},${y}`;
      })
      .join(' ');
  }

  get fillPoints(): string {
    const pts = this.linePoints.split(' ').filter(Boolean);
    if (!pts.length) return '';
    const last = pts[pts.length - 1].split(',');
    const first = pts[0].split(',');
    const lx = last[0];
    const ly = last[1];
    const fx = first[0];
    return `${pts.join(' ')} ${lx},${this.h} ${fx},${this.h}`;
  }
}
