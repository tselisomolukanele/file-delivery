import { CommonModule } from '@angular/common';
import { Component, OnInit, inject, signal } from '@angular/core';
import { RouterLink } from '@angular/router';
import { sizeFormatter } from 'human-readable'

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
    localStorage.setItem('filename', document.name);
    localStorage.setItem('link', document.link);
    window.open("/email-message.html", "_blank");
  }

  format = sizeFormatter({
    std: 'JEDEC', // 'SI' (default) | 'IEC' | 'JEDEC'
    decimalPlaces: 2,
    keepTrailingZeroes: false,
    render: (literal, symbol) => `${literal} ${symbol}B`,
  })
}
