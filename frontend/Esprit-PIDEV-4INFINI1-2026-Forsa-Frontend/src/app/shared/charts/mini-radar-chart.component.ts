import { ChangeDetectionStrategy, Component, Input } from '@angular/core';

export interface RadarDatum {
  label: string;
  value: number;
  max: number;
}

@Component({
  selector: 'app-mini-radar-chart',
  standalone: true,
  changeDetection: ChangeDetectionStrategy.OnPush,
  template: `
    <svg [attr.viewBox]="'0 0 ' + size + ' ' + size" class="chart">
      <polygon [attr.points]="gridPoly" fill="none" stroke="var(--color-border)" />
      <polygon
        [attr.points]="dataPoly"
        [attr.fill]="fill"
        [attr.fill-opacity]="fillOpacity"
        [attr.stroke]="stroke"
        stroke-width="2"
      />
      @for (l of labels; track l.t) {
        <text [attr.x]="l.x" [attr.y]="l.y" class="lbl" text-anchor="middle" dominant-baseline="middle">{{ l.t }}</text>
      }
    </svg>
  `,
  styles: `
    .chart {
      width: 100%;
      height: 100%;
      display: block;
    }
    .lbl {
      font-size: 10px;
      fill: var(--color-muted-foreground);
      font-family: var(--font-sans);
    }
  `,
})
export class MiniRadarChartComponent {
  @Input() data: RadarDatum[] = [];
  @Input() size = 320;
  @Input() stroke = 'var(--color-primary)';
  @Input() fill = 'var(--color-primary)';
  @Input() fillOpacity = 0.28;

  private get cx(): number {
    return this.size / 2;
  }
  private get cy(): number {
    return this.size / 2;
  }
  private get r(): number {
    return this.size * 0.36;
  }

  get gridPoly(): string {
    const n = Math.max(this.data.length, 3);
    return Array.from({ length: n }, (_, i) => this.vertex(i, n, this.r)).join(' ');
  }

  get dataPoly(): string {
    const n = Math.max(this.data.length, 1);
    return this.data
      .map((d, i) => this.vertex(i, n, (d.value / d.max) * this.r))
      .join(' ');
  }

  get labels(): { x: number; y: number; t: string }[] {
    const n = Math.max(this.data.length, 1);
    const pad = 18;
    return this.data.map((d, i) => {
      const angle = (-Math.PI / 2 + (2 * Math.PI * i) / n) as number;
      const x = this.cx + Math.cos(angle) * (this.r + pad);
      const y = this.cy + Math.sin(angle) * (this.r + pad);
      return { x, y, t: d.label };
    });
  }

  private vertex(i: number, n: number, radius: number): string {
    const angle = -Math.PI / 2 + (2 * Math.PI * i) / n;
    const x = this.cx + Math.cos(angle) * radius;
    const y = this.cy + Math.sin(angle) * radius;
    return `${x},${y}`;
  }
}
