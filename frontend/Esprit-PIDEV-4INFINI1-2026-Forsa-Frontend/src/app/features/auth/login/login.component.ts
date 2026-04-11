import { Component } from '@angular/core';
import { RouterLink } from '@angular/router';
import { ForsaLogoComponent } from '../../../shared/branding/forsa-logo.component';
import { ForsaButtonComponent } from '../../../shared/ui/forsa-button/forsa-button.component';
import { ForsaCardComponent } from '../../../shared/ui/forsa-card/forsa-card.component';
import { ForsaIconComponent } from '../../../shared/ui/forsa-icon/forsa-icon.component';
import { ForsaInputDirective } from '../../../shared/directives/forsa-input.directive';

@Component({
  selector: 'app-login',
  standalone: true,
  imports: [RouterLink, ForsaLogoComponent, ForsaButtonComponent, ForsaCardComponent, ForsaIconComponent, ForsaInputDirective],
  templateUrl: './login.component.html',
  styleUrl: './login.component.css',
})
export class LoginComponent {}
