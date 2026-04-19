import { Component, OnInit, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule, Router } from '@angular/router';
import { PremiumPaymentService } from '../../shared/services/premium-payment.service';
import { PremiumPayment } from '../../shared/models/insurance.models';
import { PaymentStatus } from '../../shared/enums/insurance.enums';

@Component({
  selector: 'app-client-my-payments',
  standalone: true,
  imports: [CommonModule, RouterModule],
  templateUrl: './client-my-payments.component.html',
  styleUrls: ['./client-my-payments.component.css']
})
export class ClientMyPaymentsComponent implements OnInit {
  private readonly paymentService = inject(PremiumPaymentService);
  private readonly router = inject(Router);

  payments: PremiumPayment[] = [];
  isLoading = true;

  ngOnInit() {
    this.loadPayments();
    this.checkReturnStatus();
  }

  checkReturnStatus() {
    const urlParams = new URLSearchParams(window.location.search);
    const paymentId = urlParams.get('payment_id');
    const status = urlParams.get('status');

    if (paymentId && status === 'success') {
      this.paymentService.getById(Number(paymentId)).subscribe({
        next: (payment) => {
          if (payment.status !== PaymentStatus.PAID) {
            const updatedPayment: PremiumPayment = {
              ...payment,
              status: PaymentStatus.PAID,
              paidDate: new Date().toISOString().split('T')[0],
              transactionId: Math.floor(Math.random() * 10000000)
            };
            this.paymentService.update(updatedPayment).subscribe({
              next: () => {
                alert('Payment successful! Your record has been updated.');
                this.loadPayments();
              },
              error: (err) => console.error('Failed to update payment status', err)
            });
          }
        }
      });
      // Clear URL params to avoid re-triggering on refresh
      window.history.replaceState({}, document.title, window.location.pathname);
    }
  }

  loadPayments() {
    this.isLoading = true;
    this.paymentService.getMyPayments().subscribe({
      next: (data) => {
        // Sort by due date ascending
        this.payments = data.sort((a, b) => {
          return new Date(a.dueDate || '').getTime() - new Date(b.dueDate || '').getTime();
        });
        this.isLoading = false;
      },
      error: (err) => {
        console.error(err);
        this.isLoading = false;
      }
    });
  }

  payNow(payment: PremiumPayment) {
    console.log('payNow called for payment:', payment);
    if (!payment.id) {
      console.warn('Payment ID is missing!');
      return;
    }
    
    const paymentData = {
      amount: Math.round((payment.amount || 0) * 100), // cents
      currency: 'usd',
      productName: `Premium Installment - Due: ${payment.dueDate}`,
      successUrl: `${window.location.origin}${window.location.pathname}?status=success&payment_id=${payment.id}`,
      cancelUrl: `${window.location.origin}${window.location.pathname}?status=cancel`
    };

    console.log('Sending payment data to backend:', paymentData);

    this.paymentService.createStripeSession(paymentData).subscribe({
      next: (res: any) => {
        console.log('Received response from backend:', res);
        if (res.sessionUrl) {
          console.log('Redirecting to:', res.sessionUrl);
          window.location.href = res.sessionUrl;
        } else if (res.error) {
          console.error('Backend returned error:', res.error);
          alert('Payment Error: ' + res.error);
        } else {
          console.error('Unexpected response format:', res);
          alert('An unexpected error occurred. Please check console.');
        }
      },
      error: (err) => {
        console.error('Stripe session creation failed', err);
        alert('Failed to initialize Stripe payment. Please try again.');
      }
    });
  }
}
