import { Directive, EventEmitter, HostBinding, HostListener, Output } from '@angular/core';

@Directive({
  selector: 'input[forsaInput], textarea[forsaInput]',
  standalone: true,
})
export class ForsaInputDirective {
  @HostBinding('class') hostClass = 'forsa-input';

  @Output() readonly forsaInputChange = new EventEmitter<string>();

  @HostListener('input', ['$event'])
  onNativeInput(ev: Event): void {
    const el = ev.target as HTMLInputElement | HTMLTextAreaElement;
    this.forsaInputChange.emit(el.value);
  }
}
