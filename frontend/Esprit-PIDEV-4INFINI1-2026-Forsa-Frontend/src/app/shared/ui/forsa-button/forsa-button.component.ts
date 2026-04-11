import {
  AfterViewInit,
  Component,
  ElementRef,
  EventEmitter,
  HostBinding,
  Input,
  Output,
  ViewChild,
  inject,
} from '@angular/core';
import { Router } from '@angular/router';

@Component({
  selector: 'app-forsa-button',
  standalone: true,
  imports: [],
  templateUrl: './forsa-button.component.html',
  styleUrls: ['./forsa-button.component.css'],
})
export class ForsaButtonComponent implements AfterViewInit {
  private readonly router = inject(Router);

  @ViewChild('actionButton', { read: ElementRef }) private actionButtonRef?: ElementRef<HTMLButtonElement>;

  @HostBinding('class.forsa-btn--full-width')
  get hostFullWidth(): boolean {
    return this.widthFull;
  }

  @Input() variant: 'default' | 'outline' | 'ghost' | 'secondary' | 'destructive' = 'default';
  @Input() size: 'sm' | 'md' | 'lg' | 'icon' = 'md';
  @Input() type: 'button' | 'submit' = 'button';
  @Input() link: string | null = null;
  @Input() external = false;
  @Input() disabled = false;
  @Input() widthFull = false;
  @Input() justifyStart = false;
  /** Merged onto the inner button element. */
  @Input() extraClass = '';

  /** Use for icon-only buttons (no visible text). */
  @Input() accessibleName: string | null = null;

  @Output() clicked = new EventEmitter<MouseEvent>();

  get isExternalLink(): boolean {
    return this.external || (!!this.link && /^https?:\/\//i.test(this.link));
  }

  get classes(): string {
    const parts = [
      `variant-${this.variant}`,
      `size-${this.size}`,
      this.widthFull ? 'width-full' : '',
      this.justifyStart ? 'justify-start' : '',
      this.extraClass,
    ];
    return parts.filter(Boolean).join(' ');
  }

  ngAfterViewInit(): void {
    this.syncButtonAccessibleName();
  }

  private syncButtonAccessibleName(): void {
    const btn = this.actionButtonRef?.nativeElement;
    if (!btn) return;

    const explicit = this.accessibleName?.trim();
    if (explicit) {
      btn.setAttribute('aria-label', explicit);
      btn.setAttribute('title', explicit);
      return;
    }

    const fromContent = btn.textContent?.replace(/\s+/g, ' ').trim() ?? '';
    if (fromContent) {
      btn.setAttribute('aria-label', fromContent);
      btn.removeAttribute('title');
      return;
    }

    if (this.link) {
      const hint = this.isExternalLink ? 'Open link' : `Go to ${this.link}`;
      btn.setAttribute('aria-label', hint);
      btn.setAttribute('title', hint);
      return;
    }

    btn.setAttribute('aria-label', 'Button');
    btn.removeAttribute('title');
  }

  onHostClick(ev: MouseEvent): void {
    if (this.disabled) return;
    if (!this.link) {
      this.clicked.emit(ev);
      return;
    }
    if (ev.defaultPrevented) return;
    if (ev.button !== 0) return;

    if (this.isExternalLink) {
      window.open(this.link, '_blank', 'noopener,noreferrer');
      return;
    }

    if (ev.ctrlKey || ev.metaKey) {
      window.open(this.link, '_blank');
      return;
    }
    if (ev.shiftKey || ev.altKey) return;

    void this.router.navigateByUrl(this.link);
  }
}
