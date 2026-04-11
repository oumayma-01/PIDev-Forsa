import { Component } from '@angular/core';
import { RouterLink } from '@angular/router';
import { ForsaLogoComponent } from '../../shared/branding/forsa-logo.component';
import { ForsaButtonComponent } from '../../shared/ui/forsa-button/forsa-button.component';
import { ForsaIconComponent } from '../../shared/ui/forsa-icon/forsa-icon.component';

@Component({
  selector: 'app-landing-page',
  standalone: true,
  imports: [RouterLink, ForsaLogoComponent, ForsaButtonComponent, ForsaIconComponent],
  templateUrl: './landing-page.component.html',
  styleUrl: './landing-page.component.css',
})
export class LandingPageComponent {
  readonly features = [
    {
      title: 'Smart Credit',
      description: 'AI-powered credit scoring and instant approval workflows.',
      icon: 'zap' as const,
      tone: 'blue',
    },
    {
      title: 'Digital Wallet',
      description: 'Secure, multi-currency wallet with real-time transaction tracking.',
      icon: 'wallet' as const,
      tone: 'blue2',
    },
    {
      title: 'AI Risk Analysis',
      description: 'Predictive modeling to safeguard your financial future.',
      icon: 'brain' as const,
      tone: 'cyan',
    },
    {
      title: 'Insurance System',
      description: 'Comprehensive coverage tailored to your unique lifestyle.',
      icon: 'shield' as const,
      tone: 'blue3',
    },
  ];
}
