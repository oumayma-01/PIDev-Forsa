import { Pipe, PipeTransform } from '@angular/core';
import type { PartnerType } from '../../../core/models/forsa.models';
import { PARTNER_TYPE_LABELS } from './partner-type-labels';

@Pipe({
  name: 'partnerTypeLabel',
  standalone: true,
})
export class PartnerTypeLabelPipe implements PipeTransform {
  transform(value: PartnerType): string {
    return PARTNER_TYPE_LABELS[value] ?? value;
  }
}
