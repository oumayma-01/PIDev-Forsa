import { NgTemplateOutlet } from '@angular/common';
import {
  Component,
  ContentChild,
  EventEmitter,
  Input,
  Output,
  TemplateRef,
} from '@angular/core';
import { ForsaButtonComponent } from '../forsa-button/forsa-button.component';
import { ForsaIconComponent } from '../forsa-icon/forsa-icon.component';
import type { ForsaDataTablePageEvent, ForsaTableColumn } from './forsa-data-table.types';

@Component({
  selector: 'app-forsa-data-table',
  standalone: true,
  imports: [NgTemplateOutlet, ForsaButtonComponent, ForsaIconComponent],
  templateUrl: './forsa-data-table.component.html',
  styleUrl: './forsa-data-table.component.css',
})
export class ForsaDataTableComponent {
  /** Column metadata for the shared header row */
  @Input({ required: true }) columns: ForsaTableColumn[] = [];

  /** Rows for the current page (parent slices from full dataset) */
  @Input() rows: unknown[] = [];

  @Input() totalCount = 0;

  @Input() pageIndex = 0;

  @Input() pageSize = 10;

  @Input() pageSizeOptions: ReadonlyArray<number> = [5, 10, 25, 50];

  @Input() loading = false;

  /** When true, no outer frame — use inside `app-forsa-card` + `card-pad-lg` like user management */
  @Input() embedded = false;

  @Input() emptyTitle = 'No data';

  @Input() emptyDescription = '';

  @Input() ariaLabel = 'Data table';

  @Output() pageChange = new EventEmitter<ForsaDataTablePageEvent>();

  @ContentChild('forsaTableBody', { read: TemplateRef })
  bodyTemplate?: TemplateRef<{ $implicit: unknown[]; rows: unknown[] }>;

  get totalPages(): number {
    const ps = Math.max(1, this.pageSize);
    return Math.max(1, Math.ceil(this.totalCount / ps));
  }

  get safePageIndex(): number {
    const max = Math.max(0, this.totalPages - 1);
    return Math.min(this.pageIndex, max);
  }

  get rangeStart(): number {
    if (this.totalCount === 0) {
      return 0;
    }
    return this.safePageIndex * this.pageSize + 1;
  }

  get rangeEnd(): number {
    return Math.min(this.totalCount, (this.safePageIndex + 1) * this.pageSize);
  }

  /** Show pager whenever there is data (single page still shows “Showing 1–n of n” + page size). */
  get showPagination(): boolean {
    return this.totalCount > 0;
  }

  thAlign(col: ForsaTableColumn): string {
    return col.align ?? 'left';
  }

  goPrev(): void {
    if (this.safePageIndex <= 0) {
      return;
    }
    this.emitPage(this.safePageIndex - 1, this.pageSize);
  }

  goNext(): void {
    if (this.safePageIndex >= this.totalPages - 1) {
      return;
    }
    this.emitPage(this.safePageIndex + 1, this.pageSize);
  }

  onPageSizeSelect(event: Event): void {
    const el = event.target as HTMLSelectElement;
    const next = Number(el.value);
    if (!Number.isFinite(next) || next <= 0) {
      return;
    }
    this.emitPage(0, next);
  }

  private emitPage(pageIndex: number, pageSize: number): void {
    this.pageChange.emit({ pageIndex, pageSize });
  }
}
