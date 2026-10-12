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
  fileName = '';

    ngOnInit(): void {
    this.documentService.getProfiles().subscribe({
      next: (profiles) => this.documentProfiles.set(profiles),
      error: () => this.documentProfiles.set(null),
    });
  }

  onFileSelected(event: any) {

        const file:File = event.target.files[0];

        if (file) {

            this.fileName = file.name;
            const formData = new FormData();
            formData.append("file", file);
            formData.append("filename", file.name);
            formData.append("mimetype", file.type);
            this.documentService.uploadDocument(formData).subscribe({
                next: (documents) => {
                    console.log('Documents uploaded successfully:', documents);
                },
                error: (error) => {
                    console.error('Error uploading documents:', error);
                }
            });
        }
    }
}
