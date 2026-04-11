import type { PartnerType } from '../../../core/models/forsa.models';

export const PARTNER_TYPE_LABELS: Record<PartnerType, string> = {
  PHARMACIE: 'Pharmacy',
  PARAPHARMACIE: 'Parapharmacy',
  LIBRAIRE: 'Bookstore',
  COOPERATIVE_AGRICOLE: 'Farmers’ cooperative',
  EQUIPEMENT_PROFESSIONNEL: 'Professional equipment',
  QUINCAILLERIE: 'Hardware store',
  GARAGISTE: 'Auto repair',
  SUPERMARCHE: 'Supermarket',
  TELECOM: 'Telecom',
  AUTRE: 'Other',
};
