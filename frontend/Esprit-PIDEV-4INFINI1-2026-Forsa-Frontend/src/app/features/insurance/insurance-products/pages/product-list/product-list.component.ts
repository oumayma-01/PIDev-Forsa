import { Component, inject, OnInit, signal } from '@angular/core';
import { RouterLink } from '@angular/router';
import { DecimalPipe, TitleCasePipe } from '@angular/common';
import { ForsaBadgeComponent } from '../../../../../shared/ui/forsa-badge/forsa-badge.component';
import { ForsaButtonComponent } from '../../../../../shared/ui/forsa-button/forsa-button.component';
import { ForsaCardComponent } from '../../../../../shared/ui/forsa-card/forsa-card.component';
import { ForsaIconComponent } from '../../../../../shared/ui/forsa-icon/forsa-icon.component';
import { InsuranceProductService } from '../../../shared/services/insurance-product.service';
import { InsuranceProduct } from '../../../shared/models/insurance.models';

@Component({
  selector: 'app-product-list',
  standalone: true,
  imports: [RouterLink, DecimalPipe, TitleCasePipe, ForsaBadgeComponent, ForsaButtonComponent, ForsaCardComponent, ForsaIconComponent],
  templateUrl: './product-list.component.html',
  styleUrl: './product-list.component.css',
})
export class ProductListComponent implements OnInit {
  private readonly svc = inject(InsuranceProductService);

  products = signal<InsuranceProduct[]>([]);
  loading = signal(true);
  error = signal<string | null>(null);
  deletingId = signal<number | null>(null);

  ngOnInit(): void { this.load(); }

  load(): void {
    this.loading.set(true);
    this.error.set(null);
    this.svc.getAll().subscribe({
      next: (data) => { this.products.set(data); this.loading.set(false); },
      error: (e) => { this.error.set(e.message ?? 'Failed to load products.'); this.loading.set(false); },
    });
  }

  delete(id: number): void {
    if (!confirm('Delete this product?')) return;
    this.deletingId.set(id);
    this.svc.delete(id).subscribe({
      next: () => { this.products.update((p) => p.filter((x) => x.id !== id)); this.deletingId.set(null); },
      error: (e) => { alert(e.message ?? 'Delete failed.'); this.deletingId.set(null); },
    });
  }

  typeIcon(type: string): string {
    const map: Record<string, string> = {
      HEALTH: 'heart', LIFE: 'shield-check', PROPERTY: 'home',
      ACCIDENT: 'zap', CROP: 'leaf', LIVESTOCK: 'dog', BUSINESS: 'briefcase',
    };
    return map[type] ?? 'package';
  }

  typeTone(type: string): string {
    const map: Record<string, string> = {
      HEALTH: 'rose', LIFE: 'emerald', PROPERTY: 'amber',
      ACCIDENT: 'info', CROP: 'emerald', LIVESTOCK: 'amber', BUSINESS: 'blue',
    };
    return map[type] ?? 'default';
  }
}
