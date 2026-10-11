import { CommonModule } from '@angular/common';
import { Component, OnInit, inject, signal } from '@angular/core';
import { RouterLink } from '@angular/router';

import { AuthService } from '../../auth/auth.service';
import { DocumentItem, DocumentProfile, DocumentService } from '../../services/document.service';

@Component({
  selector: 'app-account',
  standalone: true,
  imports: [CommonModule, RouterLink],
  templateUrl: './account.component.html',
  styleUrl: './account.component.scss',
})
export class AccountComponent implements OnInit {
  readonly auth = inject(AuthService);
  readonly documentService = inject(DocumentService);
  readonly user = this.auth.user;
  readonly documents = signal<DocumentItem[]>([]);

  ngOnInit(): void {
    this.documentService.getDocuments().subscribe({
      next: (items) => this.documents.set(items),
      error: () => this.documents.set([]),
    });
  }
}
