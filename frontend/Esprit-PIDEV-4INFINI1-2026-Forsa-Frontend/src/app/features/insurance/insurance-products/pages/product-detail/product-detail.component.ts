import { Component, inject, OnInit, signal } from '@angular/core';
import { ActivatedRoute, RouterLink } from '@angular/router';
import { DecimalPipe, TitleCasePipe } from '@angular/common';
import { ForsaBadgeComponent } from '../../../../../shared/ui/forsa-badge/forsa-badge.component';
import { ForsaButtonComponent } from '../../../../../shared/ui/forsa-button/forsa-button.component';
import { ForsaCardComponent } from '../../../../../shared/ui/forsa-card/forsa-card.component';
import { ForsaIconComponent } from '../../../../../shared/ui/forsa-icon/forsa-icon.component';
import { InsuranceProductService } from '../../../shared/services/insurance-product.service';
import { InsuranceProduct } from '../../../shared/models/insurance.models';

@Component({
  selector: 'app-product-detail',
  standalone: true,
  imports: [RouterLink, DecimalPipe, TitleCasePipe, ForsaBadgeComponent, ForsaButtonComponent, ForsaCardComponent, ForsaIconComponent],
  templateUrl: './product-detail.component.html',
  styleUrl: './product-detail.component.css',
})
export class ProductDetailComponent implements OnInit {
  private readonly svc = inject(InsuranceProductService);
  private readonly route = inject(ActivatedRoute);

  product = signal<InsuranceProduct | null>(null);
  loading = signal(true);
  error = signal<string | null>(null);

  ngOnInit(): void {
    const id = this.route.snapshot.paramMap.get('id');
    if (!id) { this.error.set('No ID provided'); this.loading.set(false); return; }
    this.svc.getById(+id).subscribe({
      next: (p) => { this.product.set(p); this.loading.set(false); },
      error: (e) => { this.error.set(e.message ?? 'Failed to load.'); this.loading.set(false); },
    });
  }
}
