import { Component, ElementRef, ViewChild, inject, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { Router, RouterLink } from '@angular/router';
import { AuthService } from '../../../core/services/auth.service';
import { ForsaLogoComponent } from '../../../shared/branding/forsa-logo.component';
import { ForsaButtonComponent } from '../../../shared/ui/forsa-button/forsa-button.component';
import { ForsaCardComponent } from '../../../shared/ui/forsa-card/forsa-card.component';
import { ForsaIconComponent } from '../../../shared/ui/forsa-icon/forsa-icon.component';
import { ForsaInputDirective } from '../../../shared/directives/forsa-input.directive';
import { ForsaPasswordFieldComponent } from '../../../shared/ui/forsa-password-field/forsa-password-field.component';
import { FaceCameraService } from '../../../core/services/face-camera.service';

@Component({
  selector: 'app-login',
  standalone: true,
  imports: [
    FormsModule,
    RouterLink,
    ForsaLogoComponent,
    ForsaButtonComponent,
    ForsaCardComponent,
    ForsaIconComponent,
    ForsaInputDirective,
    ForsaPasswordFieldComponent,
  ],
  templateUrl: './login.component.html',
  styleUrl: './login.component.css',
})
export class LoginComponent {
  private readonly auth = inject(AuthService);
  private readonly router = inject(Router);
  private readonly faceCamera = inject(FaceCameraService);

  username = '';
  password = '';

  readonly busy = signal(false);
  readonly googleBusy = signal(false);
  readonly biometricBusy = signal(false);
  readonly error = signal<string | null>(null);
  readonly biometricSupported = signal(this.auth.supportsBiometrics());
  readonly faceCameraOpen = signal(false);
  readonly faceBusy = signal(false);
  @ViewChild('faceVideo') faceVideoRef?: ElementRef<HTMLVideoElement>;
  private cameraVideo: HTMLVideoElement | null = null;

  signInWithGoogle(): void {
    this.googleBusy.set(true);
    this.error.set(null);
    this.auth.getGoogleLoginUrl().subscribe({
      next: (res) => {
        window.location.href = res.message;
      },
      error: (e) => {
        this.error.set(e.error?.message ?? 'Could not start Google sign-in.');
        this.googleBusy.set(false);
      },
    });
  }
  submit(): void {
    if (!this.username.trim() || !this.password) {
      this.error.set('Please enter your username and password.');
      return;
    }
    this.busy.set(true);
    this.error.set(null);
    this.auth.login(this.username.trim(), this.password).subscribe({
      next: (res) => {
        void this.router.navigateByUrl('/dashboard');
        this.busy.set(false);
      },
      error: (e) => {
        this.error.set(e.error?.message ?? 'Sign in failed.');
        this.busy.set(false);
      },
    });
  }

  signInWithBiometrics(): void {
    if (!this.biometricSupported()) {
      this.error.set('Biometric login is not supported on this browser.');
      return;
    }
    const user = this.username.trim();
    if (!user) {
      this.error.set('Enter your username first to continue with biometrics.');
      return;
    }
    this.biometricBusy.set(true);
    this.error.set(null);
    this.auth
      .loginWithPasskey(user)
      .then(() => {
        void this.router.navigateByUrl('/dashboard');
      })
      .catch((e) => {
        this.error.set(e?.error?.message ?? e?.message ?? 'Biometric sign-in failed.');
      })
      .finally(() => this.biometricBusy.set(false));
  }

  async openFaceCamera(): Promise<void> {
    this.error.set(null);
    this.faceCameraOpen.set(true);
    setTimeout(async () => {
      this.cameraVideo = this.faceVideoRef?.nativeElement ?? null;
      if (!this.cameraVideo) {
        this.faceCameraOpen.set(false);
        this.error.set('Camera view is unavailable.');
        return;
      }
      try {
        await this.faceCamera.start(this.cameraVideo);
      } catch (e) {
        this.faceCameraOpen.set(false);
        this.error.set((e as { message?: string })?.message ?? 'Cannot open camera.');
      }
    });
  }

  closeFaceCamera(): void {
    this.faceCamera.stop(this.cameraVideo ?? undefined);
    this.cameraVideo = null;
    this.faceCameraOpen.set(false);
    this.faceBusy.set(false);
  }

  captureAndLoginFace(): void {
    const user = this.username.trim();
    if (!user) {
      this.error.set('Enter your username first to continue with face login.');
      return;
    }
    if (!this.cameraVideo) {
      this.error.set('Camera is not ready.');
      return;
    }
    this.faceBusy.set(true);
    this.faceCamera
      .captureDescriptor(this.cameraVideo)
      .then((descriptor) => this.auth.loginWithFace(user, descriptor))
      .then(() => {
        this.closeFaceCamera();
        void this.router.navigateByUrl('/dashboard');
      })
      .catch((e) => {
        this.error.set(e?.error?.message ?? e?.message ?? 'Face authentication failed.');
      })
      .finally(() => this.faceBusy.set(false));
  }
}