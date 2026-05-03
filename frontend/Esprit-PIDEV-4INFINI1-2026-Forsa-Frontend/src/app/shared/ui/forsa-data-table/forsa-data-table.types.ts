export type ForsaTableColumnAlign = 'left' | 'center' | 'right';

export interface ForsaTableColumn {
  key: string;
  label: string;
  align?: ForsaTableColumnAlign;
  /** CSS width e.g. `12rem` or `20%` */
  width?: string;
}

export interface ForsaDataTablePageEvent {
  pageIndex: number;
  pageSize: number;
}
