import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule } from '@angular/router';

@Component({
  selector: 'app-front-office-hub',
  standalone: true,
  imports: [CommonModule, RouterModule],
  templateUrl: './front-office-hub.component.html',
  styleUrls: ['./front-office-hub.component.css']
})
export class FrontOfficeHubComponent {}
