import { ChangeDetectionStrategy, Component, Input } from '@angular/core';

@Component({
  selector: 'app-mini-line-chart',
  standalone: true,
  changeDetection: ChangeDetectionStrategy.OnPush,
  template: `
    <svg [attr.viewBox]="'0 0 ' + w + ' ' + h" preserveAspectRatio="none" class="chart">
      <polyline [attr.points]="gridH" fill="none" stroke="var(--color-border)" stroke-width="1" stroke-dasharray="4 4" />
      <polyline [attr.points]="linePoints" fill="none" [attr.stroke]="color" stroke-width="4" stroke-linejoin="round" stroke-linecap="round" />
      @for (p of dots; track p.x) {
        <circle [attr.cx]="p.x" [attr.cy]="p.y" r="6" [attr.fill]="color" stroke="var(--color-card)" stroke-width="2" />
      }
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
export class MiniLineChartComponent {
  @Input() points: { x: string; y: number }[] = [];
  @Input() w = 400;
  @Input() h = 200;
  @Input() color = 'var(--color-primary)';
  @Input() yMax = 100;
  @Input() yMin = 0;

  get gridH(): string {
    return `0,${this.h * 0.75} ${this.w},${this.h * 0.75}`;
  }

  get linePoints(): string {
    const n = this.points.length;
    return this.points
      .map((pt, i) => {
        const x = (i / Math.max(n - 1, 1)) * this.w;
        const span = this.yMax - this.yMin || 1;
        const y = this.h - ((pt.y - this.yMin) / span) * (this.h - 24) - 12;
        return `${x},${y}`;
      })
      .join(' ');
  }

  get dots(): { x: number; y: number }[] {
    const n = this.points.length;
    const span = this.yMax - this.yMin || 1;
    return this.points.map((pt, i) => {
      const x = (i / Math.max(n - 1, 1)) * this.w;
      const y = this.h - ((pt.y - this.yMin) / span) * (this.h - 24) - 12;
      return { x, y };
    });
  }
}
