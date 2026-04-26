import { Component, OnInit } from '@angular/core';
import { Router } from '@angular/router';

@Component({
  selector: 'app-feedback-form-legacy',
  standalone: true,
  imports: [],
  template: '',
})
export class FeedbackFormComponent implements OnInit {
  constructor(private router: Router) {}
  ngOnInit(): void {
    this.router.navigate(['/dashboard/feedback']);
  }
}
