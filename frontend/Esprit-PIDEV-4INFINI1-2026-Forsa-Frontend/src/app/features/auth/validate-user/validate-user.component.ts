import { HttpClient } from '@angular/common/http';
import { Component, inject, OnInit, signal } from '@angular/core';
import { ActivatedRoute, RouterLink } from '@angular/router';
import { environment } from '../../../../environments/environment';
import { ForsaButtonComponent } from '../../../shared/ui/forsa-button/forsa-button.component';
import { ForsaCardComponent } from '../../../shared/ui/forsa-card/forsa-card.component';
import type { MessageResponse } from '../../../core/models/auth.model';

@Component({
  selector: 'app-validate-user',
  standalone: true,
  imports: [RouterLink, ForsaCardComponent, ForsaButtonComponent],
  templateUrl: './validate-user.component.html',
  styleUrl: './validate-user.component.css',
})
export class ValidateUserComponent implements OnInit {
  private readonly http = inject(HttpClient);
  private readonly route = inject(ActivatedRoute);

  readonly message = signal<string | null>(null);
  readonly error = signal<string | null>(null);
  readonly busy = signal(true);

  ngOnInit(): void {
    const id = this.route.snapshot.paramMap.get('id');
    if (!id) {
      this.busy.set(false);
      this.error.set('Invalid verification link.');
      return;
    }
    this.http.put<MessageResponse>(`${environment.apiBaseUrl}/auth/activate/${id}`, {}).subscribe({
      next: (res) => {
        this.message.set(res.message);
        this.error.set(null);
        this.busy.set(false);
      },
      error: (e) => {
        this.error.set(e.error?.message ?? 'Verification failed.');
        this.message.set(null);
        this.busy.set(false);
      },
    });
  }
}
