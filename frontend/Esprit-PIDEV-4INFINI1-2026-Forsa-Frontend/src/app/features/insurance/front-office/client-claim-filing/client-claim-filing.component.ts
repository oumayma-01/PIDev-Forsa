import { Component, OnInit, inject, signal, computed } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ActivatedRoute, Router, RouterModule } from '@angular/router';
import { FormBuilder, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { firstValueFrom } from 'rxjs';
import { InsuranceClaimService } from '../../shared/services/insurance-claim.service';
import { InsurancePolicyService } from '../../shared/services/insurance-policy.service';
import { InsurancePolicy, InsuranceClaim, ClaimTemplate } from '../../shared/models/insurance.models';
import { ForsaCardComponent } from '../../../../shared/ui/forsa-card/forsa-card.component';
import { ForsaIconComponent } from '../../../../shared/ui/forsa-icon/forsa-icon.component';
import { ForsaButtonComponent } from '../../../../shared/ui/forsa-button/forsa-button.component';
import { ForsaBadgeComponent } from '../../../../shared/ui/forsa-badge/forsa-badge.component';

export interface UploadedFile {
  file: File;
  name: string;
  size: string;
  type: 'image' | 'pdf' | 'other';
  previewUrl: string | null;
  objectUrl: string;
}

@Component({
  selector: 'app-client-claim-filing',
  standalone: true,
  imports: [
    CommonModule,
    ReactiveFormsModule,
    RouterModule,
    ForsaCardComponent,
    ForsaIconComponent,
    ForsaButtonComponent,
    ForsaBadgeComponent
  ],
  templateUrl: './client-claim-filing.component.html',
  styleUrls: ['./client-claim-filing.component.css']
})
export class ClientClaimFilingComponent implements OnInit {
  private route = inject(ActivatedRoute);
  private router = inject(Router);
  private fb = inject(FormBuilder);
  private claimService = inject(InsuranceClaimService);
  private policyService = inject(InsurancePolicyService);

  policy = signal<InsurancePolicy | null>(null);
  loading = signal(true);
  submitting = signal(false);
  error = signal<string | null>(null);
  success = signal(false);

  currentStep = signal(1);
  claimForm!: FormGroup;
  dynamicForm!: FormGroup;
  claimTemplate = signal<ClaimTemplate | null>(null);

  // Multi-file upload: docId -> UploadedFile[]
  requiredDocsFiles = signal<Map<string, UploadedFile[]>>(new Map());
  optionalDocsFiles = signal<Map<string, UploadedFile[]>>(new Map());

  draggingOver = signal<string | null>(null);

  // Damage points for vehicle
  damagePoints = signal<{ x: number, y: number, part: string }[]>([]);

  // Policy-type icon map
  policyTypeIcon: Record<string, string> = {
    HEALTH: '❤️', LIFE: '🌟', VEHICLE: '🚗', PROPERTY: '🏠',
    CROP: '🌾', LIVESTOCK: '🐄', BUSINESS: '💼', GENERAL: '📋'
  };

  accidentTypes = signal<{ id: string, label: string, icon: string, color: string }[]>([]);

  readonly steps = [
    { num: 1, label: 'Basics', icon: 'zap' },
    { num: 2, label: 'Specifics', icon: 'layout-dashboard' },
    { num: 3, label: 'Documents', icon: 'download' },
    { num: 4, label: 'Review', icon: 'shield-check' }
  ];

  ngOnInit() {
    const policyId = this.route.snapshot.params['policyId'];
    this.claimForm = this.fb.group({
      incidentDate: [new Date().toISOString().split('T')[0], Validators.required],
      claimAmount: ['', [Validators.required, Validators.min(1)]],
      accidentType: ['', Validators.required],
      severity: ['MEDIUM', Validators.required],
      description: ['', [Validators.required, Validators.minLength(30)]]
    });
    this.dynamicForm = this.fb.group({});

    if (policyId) {
      this.loadPolicy(policyId);
    }
  }

  loadPolicy(id: number) {
    this.policyService.getById(id).subscribe({
      next: (p: InsurancePolicy) => {
        this.policy.set(p);
        const pType = this.resolvePolicyType(p.insuranceProduct?.policyType || '');
        this.fetchTemplate(pType);
      },
      error: () => {
        this.error.set('Could not load policy details.');
        this.loading.set(false);
      }
    });
  }

  resolvePolicyType(raw: string): string {
    const t = (raw || '').toUpperCase();
    if (t.includes('HEALTH') || t.includes('MEDICAL')) return 'HEALTH';
    if (t.includes('LIFE')) return 'LIFE';
    if (t.includes('HOME') || t.includes('PROPERTY')) return 'PROPERTY';
    if (t.includes('CROP') || t.includes('AGRICULTURE')) return 'CROP';
    if (t.includes('LIVESTOCK') || t.includes('ANIMAL')) return 'LIVESTOCK';
    if (t.includes('BUSINESS') || t.includes('COMMERCIAL')) return 'BUSINESS';
    if (t.includes('ACCIDENT') || t.includes('MOTOR') || t.includes('AUTO') || t.includes('VEHICLE')) return 'VEHICLE';
    return 'GENERAL';
  }

  fetchTemplate(pType: string) {
    this.claimService.getClaimTemplate(pType).subscribe({
      next: (template) => {
        this.claimTemplate.set(template);
        this.buildDynamicForm(template);
        this.setAccidentTypes(pType);
        this.loading.set(false);
      },
      error: () => {
        this.error.set('Could not load claim template.');
        this.loading.set(false);
      }
    });
  }

  buildDynamicForm(template: ClaimTemplate) {
    const group: Record<string, any> = {};
    template.fields.forEach(field => {
      group[field.name] = [field.defaultValue || '', field.required ? [Validators.required] : []];
    });
    this.dynamicForm = this.fb.group(group);
  }

  setAccidentTypes(pType: string) {
    const typeMap: Record<string, { id: string, label: string, icon: string, color: string }[]> = {
      HEALTH: [
        { id: 'illness', label: 'Illness', icon: 'heart', color: '#e63946' },
        { id: 'injury', label: 'Physical Injury', icon: 'zap', color: '#e07c24' },
        { id: 'emergency', label: 'Emergency', icon: 'alert-circle', color: '#c1121f' },
        { id: 'surgery', label: 'Surgery', icon: 'shield-check', color: '#2a9d8f' },
        { id: 'routine', label: 'Routine Checkup', icon: 'history', color: '#457b9d' },
        { id: 'other', label: 'Other', icon: 'more-horizontal', color: '#6c757d' }
      ],
      LIFE: [
        { id: 'death', label: 'Death Claim', icon: 'heart', color: '#6a4c93' },
        { id: 'terminal', label: 'Terminal Illness', icon: 'alert-circle', color: '#c1121f' },
        { id: 'disability', label: 'Total Disability', icon: 'shield-alert', color: '#e07c24' },
        { id: 'other', label: 'Other', icon: 'more-horizontal', color: '#6c757d' }
      ],
      VEHICLE: [
        { id: 'collision', label: 'Collision / Accident', icon: 'car', color: '#e63946' },
        { id: 'theft', label: 'Theft / Burglary', icon: 'shield', color: '#6a4c93' },
        { id: 'fire', label: 'Fire Damage', icon: 'zap', color: '#e07c24' },
        { id: 'natural', label: 'Natural Disaster', icon: 'trending-down', color: '#457b9d' },
        { id: 'vandalism', label: 'Vandalism', icon: 'alert-circle', color: '#2a9d8f' },
        { id: 'other', label: 'Other', icon: 'more-horizontal', color: '#6c757d' }
      ],
      PROPERTY: [
        { id: 'fire', label: 'Fire / Explosion', icon: 'zap', color: '#e63946' },
        { id: 'water', label: 'Water / Flood', icon: 'trending-down', color: '#457b9d' },
        { id: 'theft', label: 'Theft / Burglary', icon: 'shield', color: '#6a4c93' },
        { id: 'natural', label: 'Natural Disaster', icon: 'alert-circle', color: '#2a9d8f' },
        { id: 'vandalism', label: 'Vandalism', icon: 'shield-alert', color: '#e07c24' },
        { id: 'other', label: 'Other', icon: 'more-horizontal', color: '#6c757d' }
      ],
      CROP: [
        { id: 'drought', label: 'Drought', icon: 'sun', color: '#e07c24' },
        { id: 'flood', label: 'Flood Damage', icon: 'trending-down', color: '#457b9d' },
        { id: 'pest', label: 'Pest Infestation', icon: 'alert-circle', color: '#2a9d8f' },
        { id: 'hail', label: 'Hail / Frost', icon: 'zap', color: '#6a4c93' },
        { id: 'fire', label: 'Fire', icon: 'trending-up', color: '#e63946' },
        { id: 'other', label: 'Other', icon: 'more-horizontal', color: '#6c757d' }
      ],
      LIVESTOCK: [
        { id: 'disease', label: 'Disease Outbreak', icon: 'alert-circle', color: '#e63946' },
        { id: 'predator', label: 'Predator Attack', icon: 'shield-alert', color: '#6a4c93' },
        { id: 'accident', label: 'Accident / Injury', icon: 'zap', color: '#e07c24' },
        { id: 'natural', label: 'Natural Disaster', icon: 'trending-down', color: '#457b9d' },
        { id: 'other', label: 'Other', icon: 'more-horizontal', color: '#6c757d' }
      ],
      BUSINESS: [
        { id: 'interruption', label: 'Business Interruption', icon: 'trending-down', color: '#e63946' },
        { id: 'liability', label: 'Liability Claim', icon: 'shield-alert', color: '#6a4c93' },
        { id: 'property', label: 'Property Damage', icon: 'home', color: '#e07c24' },
        { id: 'theft', label: 'Theft / Fraud', icon: 'shield', color: '#2a9d8f' },
        { id: 'other', label: 'Other', icon: 'more-horizontal', color: '#6c757d' }
      ],
      GENERAL: [
        { id: 'general', label: 'General Incident', icon: 'alert-circle', color: '#457b9d' },
        { id: 'theft', label: 'Theft', icon: 'shield', color: '#6a4c93' },
        { id: 'damage', label: 'Physical Damage', icon: 'zap', color: '#e07c24' },
        { id: 'other', label: 'Other', icon: 'more-horizontal', color: '#6c757d' }
      ]
    };
    const types = typeMap[pType] || typeMap['GENERAL'];
    this.accidentTypes.set(types);
    this.claimForm.get('accidentType')?.setValue(types[0].id);
  }

  nextStep() {
    if (this.currentStep() === 1) {
      const d = this.claimForm.get('incidentDate');
      const a = this.claimForm.get('claimAmount');
      const t = this.claimForm.get('accidentType');
      if (d?.invalid || a?.invalid || t?.invalid) {
        d?.markAsTouched(); a?.markAsTouched(); t?.markAsTouched();
        return;
      }
    }
    if (this.currentStep() === 2 && this.dynamicForm?.invalid) {
      this.dynamicForm.markAllAsTouched();
      return;
    }
    if (this.currentStep() < 4) this.currentStep.update(s => s + 1);
  }

  prevStep() {
    if (this.currentStep() > 1) this.currentStep.update(s => s - 1);
  }

  // ─── File Upload (multi-file per slot) ───────────────────────────────────────
  onFilesSelected(event: Event, docId: string, isRequired: boolean) {
    const input = event.target as HTMLInputElement;
    if (!input.files?.length) return;
    Array.from(input.files).forEach(f => this.addFile(f, docId, isRequired));
    input.value = '';
  }

  onDrop(event: DragEvent, docId: string, isRequired: boolean) {
    event.preventDefault();
    this.draggingOver.set(null);
    if (!event.dataTransfer?.files?.length) return;
    Array.from(event.dataTransfer.files).forEach(f => this.addFile(f, docId, isRequired));
  }

  onDragOver(event: DragEvent, docId: string) {
    event.preventDefault();
    this.draggingOver.set(docId);
  }

  onDragLeave() { this.draggingOver.set(null); }

  private addFile(file: File, docId: string, isRequired: boolean) {
    const objectUrl = URL.createObjectURL(file);
    const isImg = file.type.startsWith('image/');
    const isPdf = file.type === 'application/pdf';
    const uploaded: UploadedFile = {
      file,
      name: file.name,
      size: this.formatBytes(file.size),
      type: isImg ? 'image' : isPdf ? 'pdf' : 'other',
      previewUrl: isImg ? objectUrl : null,
      objectUrl
    };

    if (isRequired) {
      this.requiredDocsFiles.update(m => {
        const clone = new Map(m);
        clone.set(docId, [...(clone.get(docId) || []), uploaded]);
        return clone;
      });
    } else {
      this.optionalDocsFiles.update(m => {
        const clone = new Map(m);
        clone.set(docId, [...(clone.get(docId) || []), uploaded]);
        return clone;
      });
    }
  }

  removeFile(docId: string, index: number, isRequired: boolean) {
    if (isRequired) {
      this.requiredDocsFiles.update(m => {
        const clone = new Map(m);
        const arr = [...(clone.get(docId) || [])];
        URL.revokeObjectURL(arr[index].objectUrl);
        arr.splice(index, 1);
        clone.set(docId, arr);
        return clone;
      });
    } else {
      this.optionalDocsFiles.update(m => {
        const clone = new Map(m);
        const arr = [...(clone.get(docId) || [])];
        URL.revokeObjectURL(arr[index].objectUrl);
        arr.splice(index, 1);
        clone.set(docId, arr);
        return clone;
      });
    }
  }

  openFile(uf: UploadedFile) { window.open(uf.objectUrl, '_blank'); }

  getFiles(docId: string, isRequired: boolean): UploadedFile[] {
    return (isRequired ? this.requiredDocsFiles() : this.optionalDocsFiles()).get(docId) || [];
  }

  formatBytes(bytes: number): string {
    if (bytes < 1024) return bytes + ' B';
    if (bytes < 1048576) return (bytes / 1024).toFixed(1) + ' KB';
    return (bytes / 1048576).toFixed(1) + ' MB';
  }

  // ─── Damage Point (vehicle only) ─────────────────────────────────────────────
  addDamagePoint(event: MouseEvent) {
    const target = event.currentTarget as HTMLElement;
    const rect = target.getBoundingClientRect();
    const x = ((event.clientX - rect.left) / rect.width) * 100;
    const y = ((event.clientY - rect.top) / rect.height) * 100;
    let part = 'Body Panel';
    if (y < 30) part = 'Upper / Roof';
    if (y > 70) part = 'Lower / Chassis';
    if (x < 30) part = 'Front End';
    if (x > 70) part = 'Rear End';
    this.damagePoints.update(pts => [...pts, { x, y, part }]);
  }

  clearPoints() { this.damagePoints.set([]); }

  removeDamagePoint(index: number) {
    this.damagePoints.update(pts => pts.filter((_, j) => j !== index));
  }

  // ─── Submit ───────────────────────────────────────────────────────────────────
  async submitClaim() {
    if (this.claimForm.get('description')?.invalid) return;
    const template = this.claimTemplate();
    if (template) {
      for (const req of template.requiredDocuments) {
        if (!this.requiredDocsFiles().get(req.id)?.length) {
          this.error.set(`Please upload at least one file for: ${req.name}`);
          return;
        }
      }
    }

    this.submitting.set(true);
    this.error.set(null);
    try {
      // 4. File uploads
      let attachmentUrls: string[] = [];
      const allFiles = Array.from(this.requiredDocsFiles().values()).flat();
      
      for (const uf of allFiles) {
        const fileName = await firstValueFrom(this.claimService.uploadAttachment(uf.file));
        attachmentUrls.push(fileName);
      }
      
      const attachmentUrl = attachmentUrls.join(',');

      const claim: InsuranceClaim = {
        ...this.claimForm.value,
        claimDate: new Date().toISOString(),
        status: 'SUBMITTED' as any,
        insurancePolicy: { id: parseInt(this.policy()!.id!.toString()) },
        attachmentUrl,
        damagedPoints: JSON.stringify(this.damagePoints()),
        claimNumber: 'CLM-' + Date.now() + '-' + Math.floor(Math.random() * 1000),
        claimSubtype: template?.policyType || 'GENERAL',
        dynamicData: JSON.stringify(this.dynamicForm.value)
      };

      this.claimService.create(claim).subscribe({
        next: () => { this.success.set(true); this.submitting.set(false); },
        error: (err: any) => {
          this.error.set(err.error?.message || 'Submission failed. Please try again.');
          this.submitting.set(false);
        }
      });
    } catch (e) {
      this.error.set('File upload failed. Please try again.');
      this.submitting.set(false);
    }
  }

  goToDashboard() { this.router.navigate(['/dashboard/insurance/client/my-claims']); }

  get totalRequiredUploaded(): number {
    let count = 0;
    this.requiredDocsFiles().forEach(files => { if (files.length) count++; });
    return count;
  }

  get totalRequiredDocs(): number {
    return this.claimTemplate()?.requiredDocuments.length || 0;
  }

  get policyEmoji(): string {
    const pt = this.resolvePolicyType(this.policy()?.insuranceProduct?.policyType || '');
    return this.policyTypeIcon[pt] || '📋';
  }
}
