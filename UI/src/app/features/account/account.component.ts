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

  download(documentId: number, fileName: string) {
    this.customerService.download(documentId)
    .subscribe({
      next: (blob: Blob) => {
        // Create a local URL pointing to the binary Blob object
        const objectUrl = window.URL.createObjectURL(blob);
        
        // Setup an invisible anchor element to trigger the download
        const anchor = document.createElement('a');
        anchor.href = objectUrl;
        anchor.download = fileName;
        
        // Programmatically trigger a click event to launch the browser's download
        anchor.click();
        
        // Clean up memory allocations by revoking the local URL object
        window.URL.revokeObjectURL(objectUrl);
      },
      error: (error) => {
        console.error('File download failed:', error);
      }
    });
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
