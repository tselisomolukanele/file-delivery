import { CommonModule } from '@angular/common';
import { Component, OnInit, inject, signal } from '@angular/core';

import { AuthService } from '../../auth/auth.service';
import { DocumentItem, DocumentProfile, DocumentService } from '../../services/document.service';

@Component({
  selector: 'app-accounts',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './accounts.component.html',
  styleUrl: './accounts.component.scss',
})
export class AccountsComponent implements OnInit {
  readonly auth = inject(AuthService);
  readonly documentService = inject(DocumentService);
  readonly users = this.auth.users;
  readonly documentProfiles = signal<DocumentProfile[] | null>(null);

    ngOnInit(): void {
    this.documentService.getProfiles().subscribe({
      next: (profiles) => this.documentProfiles.set(profiles),
      error: () => this.documentProfiles.set(null),
    });
  }
}
