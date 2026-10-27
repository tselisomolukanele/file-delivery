import { CommonModule } from '@angular/common';
import { Component, OnInit, inject, signal } from '@angular/core';
import { RouterLink } from '@angular/router';
import { sizeFormatter } from 'human-readable'
import { ToastrService } from 'ngx-toastr';

import { AuthService } from '../../auth/auth.service';
import { DocumentItem } from '../../services/document.service';
import { CustomerService } from '../../services/cutomer.service';

@Component({
  selector: 'app-account',
  standalone: true,
  imports: [ CommonModule, RouterLink ],
  templateUrl: './account.component.html',
  styleUrl: './account.component.scss',
})
export class AccountComponent implements OnInit {
  readonly auth = inject(AuthService);
  readonly toastr = inject(ToastrService);
  readonly customerService = inject(CustomerService);
  readonly user = this.auth.user;
  readonly documents = signal<DocumentItem[]>([]);

  ngOnInit(): void {
    this.customerService.getDocuments().subscribe({
      next: (items) => this.documents.set(items),
      error: () => this.documents.set([]),
    });
  }

  goToLink(document: DocumentItem) {
    window.open(`/email-message.html?documentId=${document.id}&username=${this.user()?.username}`, "_blank");
  }

  getOTP() {
    this.customerService.getOTP().subscribe({
      next: (otp) => 
        this.toastr.info(otp, `OTP`, {
          timeOut: 5000,
          closeButton: true,
          progressBar: true,
        })
    });
  }

  format = sizeFormatter({
    std: 'JEDEC', // 'SI' (default) | 'IEC' | 'JEDEC'
    decimalPlaces: 2,
    keepTrailingZeroes: false,
    render: (literal, symbol) => `${literal} ${symbol}B`,
  })
}
