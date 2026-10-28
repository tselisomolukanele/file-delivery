import { CommonModule } from '@angular/common';
import { Component, OnInit, inject, signal } from '@angular/core';

import { AuthService } from '../../auth/auth.service';
import { DocumentProfile, DocumentService } from '../../services/document.service';
import { CustomerService } from '../../services/customer.service';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';

@Component({
  selector: 'app-accounts',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule],
  templateUrl: './accounts.component.html',
  styleUrl: './accounts.component.scss',
})
export class AccountsComponent implements OnInit {
  readonly auth = inject(AuthService);
  readonly documentService = inject(DocumentService);
  readonly customerService = inject(CustomerService);
  readonly users = this.auth.users;
  readonly documentProfiles = signal<DocumentProfile[] | null>(null);
  fileName = '';
  fb = new FormBuilder();
  customerForm = this.fb.group({
    name: ['', Validators.required]
  });

  ngOnInit(): void {
    this.customerService.getProfiles().subscribe({
      next: (profiles) => this.documentProfiles.set(profiles),
      error: () => this.documentProfiles.set(null),
    });
  }

  onFileSelected(customerName: string, event: any) {

      const file:File = event.target.files[0];

      if (file) {

          this.fileName = file.name;
          const formData = new FormData();
          formData.append("file", file);
          formData.append("filename", file.name);
          formData.append("customerName", customerName);
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

  onFormSubmit() {
    this.customerService.addCustomer(this.customerForm.value)
    .subscribe({
      next: () => {
        this.customerService.getProfiles().subscribe({
          next: (profiles) => this.documentProfiles.set(profiles),
          error: () => this.documentProfiles.set(null),
        });
        this.customerForm.reset();
      },
      error: (error) => {
        console.error('Error creating customer:', error);
      }
    });
  }
}
