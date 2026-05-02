import { Component, ElementRef, EventEmitter, Output, ViewChild, AfterViewInit, HostListener } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { ForsaButtonComponent } from '../forsa-button/forsa-button.component';

@Component({
  selector: 'app-forsa-signature',
  standalone: true,
  imports: [CommonModule, FormsModule, ForsaButtonComponent],
  template: `
    <div class="sig-container">
      <div class="sig-tabs">
        <button [class.active]="mode === 'draw'" (click)="mode = 'draw'">Draw Signature</button>
        <button [class.active]="mode === 'type'" (click)="mode = 'type'">Type Name</button>
      </div>

      <div class="sig-body">
        <div [hidden]="mode !== 'draw'" class="canvas-box">
          <canvas #sigCanvas width="400" height="150" (mousedown)="startDrawing($event)" (mousemove)="draw($event)" (mouseup)="stopDrawing()" (mouseleave)="stopDrawing()"></canvas>
          <button class="clear-btn" (click)="clearCanvas()">Clear</button>
        </div>

        <div [hidden]="mode !== 'type'" class="type-box">
          <input type="text" [(ngModel)]="typedName" placeholder="Type your full name here" (paste)="$event.preventDefault()" (keydown)="onKeyDown($event)" />
          <p class="hint">Type your name to digitally sign. Copy-paste is disabled.</p>
        </div>
      </div>

      <div class="sig-footer">
        <app-forsa-button variant="default" (click)="confirm()">Confirm Signature</app-forsa-button>
      </div>
    </div>
  `,
  styles: [`
    .sig-container { background: #fff; border-radius: 1rem; overflow: hidden; border: 1px solid #e2e8f0; }
    .sig-tabs { display: flex; border-bottom: 1px solid #e2e8f0; background: #f8fafc; }
    .sig-tabs button { flex: 1; padding: 0.75rem; border: none; background: none; font-size: 0.875rem; font-weight: 600; color: #64748b; cursor: pointer; transition: all 0.2s; }
    .sig-tabs button.active { color: #0f172a; background: #fff; border-bottom: 2px solid #10b981; }
    .sig-body { padding: 1.5rem; min-height: 180px; }
    .canvas-box { position: relative; border: 2px dashed #cbd5e1; border-radius: 0.5rem; }
    canvas { width: 100%; height: 150px; cursor: crosshair; touch-action: none; }
    .clear-btn { position: absolute; top: 0.5rem; right: 0.5rem; font-size: 0.75rem; padding: 0.25rem 0.5rem; border: 1px solid #cbd5e1; border-radius: 0.25rem; background: #fff; color: #64748b; cursor: pointer; }
    .type-box input { width: 100%; padding: 1rem; border: 2px solid #e2e8f0; border-radius: 0.5rem; font-size: 1.5rem; font-family: 'Times New Roman', serif; font-style: italic; color: #0f172a; outline: none; }
    .type-box input:focus { border-color: #10b981; }
    .hint { font-size: 0.75rem; color: #94a3b8; margin-top: 0.5rem; }
    .sig-footer { padding: 1rem; border-top: 1px solid #e2e8f0; text-align: right; background: #f8fafc; }
  `]
})
export class DigitalSignatureComponent implements AfterViewInit {
  @ViewChild('sigCanvas') canvasRef!: ElementRef<HTMLCanvasElement>;
  @Output() onConfirm = new EventEmitter<string>();

  mode: 'draw' | 'type' = 'draw';
  typedName = '';
  private ctx!: CanvasRenderingContext2D;
  private isDrawing = false;

  ngAfterViewInit() {
    this.ctx = this.canvasRef.nativeElement.getContext('2d')!;
    this.ctx.lineWidth = 2;
    this.ctx.lineCap = 'round';
    this.ctx.strokeStyle = '#000';
  }

  startDrawing(e: MouseEvent) {
    this.isDrawing = true;
    this.ctx.beginPath();
    this.ctx.moveTo(e.offsetX, e.offsetY);
  }

  draw(e: MouseEvent) {
    if (!this.isDrawing) return;
    this.ctx.lineTo(e.offsetX, e.offsetY);
    this.ctx.stroke();
  }

  stopDrawing() {
    this.isDrawing = false;
  }

  clearCanvas() {
    this.ctx.clearRect(0, 0, this.canvasRef.nativeElement.width, this.canvasRef.nativeElement.height);
  }

  onKeyDown(e: KeyboardEvent) {
    // Basic prevention of copy-paste already handled by (paste)
  }

  confirm() {
    if (this.mode === 'draw') {
      const dataUrl = this.canvasRef.nativeElement.toDataURL('image/png');
      this.onConfirm.emit(dataUrl);
    } else {
      if (!this.typedName.trim()) return;
      this.onConfirm.emit(this.typedName);
    }
  }
}
