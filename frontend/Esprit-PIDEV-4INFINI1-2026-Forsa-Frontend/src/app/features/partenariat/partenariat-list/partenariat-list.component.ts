import { DecimalPipe, NgFor, NgIf } from '@angular/common';
import { Component, computed, signal } from '@angular/core';
import { MOCK_PARTNERS } from '../../../core/data/mock-data';
import type { Partner, PartnerStatus, PartnerType } from '../../../core/models/forsa.models';
import { ForsaBadgeComponent } from '../../../shared/ui/forsa-badge/forsa-badge.component';
import { ForsaButtonComponent } from '../../../shared/ui/forsa-button/forsa-button.component';
import { ForsaCardComponent } from '../../../shared/ui/forsa-card/forsa-card.component';
import { ForsaIconComponent } from '../../../shared/ui/forsa-icon/forsa-icon.component';
import { ForsaInputDirective } from '../../../shared/directives/forsa-input.directive';
import { PARTNER_TYPE_LABELS, PartnerTypeLabelPipe } from './partenariat-type-label-public';

@Component({
  selector: 'app-partenariat-list',
  standalone: true,
  templateUrl: './partenariat-list.component.html',
  styleUrl: './partenariat-list.component.css',
  imports: [
    DecimalPipe,
    NgFor,
    NgIf,
    ForsaBadgeComponent,
    ForsaButtonComponent,
    ForsaCardComponent,
    ForsaIconComponent,
    ForsaInputDirective,
    PartnerTypeLabelPipe,
  ],
})
export class PartenariatListComponent {
  readonly allPartners = MOCK_PARTNERS;
  readonly searchQuery = signal('');
  readonly statusFilter = signal<PartnerStatus | 'ALL'>('ALL');
  readonly typeFilter = signal<PartnerType | 'ALL'>('ALL');

  readonly filteredPartners = computed(() => {
    const q = this.searchQuery().toLowerCase().trim();
    const st = this.statusFilter();
    const ty = this.typeFilter();
    return this.allPartners.filter((p) => {
      if (st !== 'ALL' && p.status !== st) return false;
      if (ty !== 'ALL' && p.partnerType !== ty) return false;
      if (!q) return true;
      const hay = `${p.businessName} ${p.city} ${p.registrationNumber} ${p.partnerType}`.toLowerCase();
      return hay.includes(q);
    });
  });

  readonly stats = computed(() => {
    const list = this.allPartners;
    const active = list.filter((p) => p.status === 'ACTIVE').length;
    const rated = list.filter((p) => p.averageRating != null && p.averageRating > 0);
    const avg =
      rated.length === 0 ? 0 : rated.reduce((s, p) => s + (p.averageRating ?? 0), 0) / rated.length;
    const volume = list.reduce((s, p) => s + (p.totalAmountProcessed ?? 0), 0);
    return {
      total: list.length,
      active,
      avgRating: avg,
      volume,
    };
  });

  readonly avgRatingParts = computed(() => {
    const a = this.stats().avgRating;
    if (a > 0) {
      return { text: a.toFixed(1), suffix: '/5' };
    }
    return { text: '-', suffix: '' };
  });

  readonly statusOptions: { value: PartnerStatus | 'ALL'; label: string }[] = [
    { value: 'ALL', label: 'All statuses' },
    { value: 'PENDING', label: 'Pending' },
    { value: 'ACTIVE', label: 'Active' },
    { value: 'SUSPENDED', label: 'Suspended' },
    { value: 'REJECTED', label: 'Rejected' },
    { value: 'CLOSED', label: 'Closed' },
  ];

  readonly typeOptions: { value: PartnerType | 'ALL'; label: string }[] = [
    { value: 'ALL', label: 'All types' },
    ...(Object.keys(PARTNER_TYPE_LABELS) as PartnerType[]).map((value) => ({
      value,
      label: PARTNER_TYPE_LABELS[value],
    })),
  ];

  onPartnerFilterValue(value: string): void {
    this.searchQuery.set(value);
  }

  onStatusSelect(ev: Event): void {
    this.statusFilter.set((ev.target as HTMLSelectElement).value as PartnerStatus | 'ALL');
  }

  onTypeSelect(ev: Event): void {
    this.typeFilter.set((ev.target as HTMLSelectElement).value as PartnerType | 'ALL');
  }

  statusTone(status: PartnerStatus): 'success' | 'warning' | 'danger' | 'info' | 'muted' {
    switch (status) {
      case 'ACTIVE':
        return 'success';
      case 'PENDING':
        return 'warning';
      case 'SUSPENDED':
        return 'danger';
      case 'REJECTED':
        return 'danger';
      case 'CLOSED':
        return 'muted';
    }
  }

  badgeTone(badge: Partner['badge']): 'default' | 'secondary' | 'warning' | 'info' | 'muted' {
    switch (badge) {
      case 'DIAMOND':
        return 'info';
      case 'GOLD':
        return 'warning';
      case 'SILVER':
        return 'secondary';
      case 'BRONZE':
        return 'default';
      default:
        return 'muted';
    }
  }
}
