import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';

import { AuthService } from '../auth/auth.service';
import { DocumentItem, DocumentProfile } from './document.service';

@Injectable({ providedIn: 'root' })
export class CustomerService {
  private readonly http = inject(HttpClient);
  private readonly auth = inject(AuthService);
  private readonly baseUrl = 'http://localhost:8081';

  addCustomer(customer: any): Observable<any> {
    return this.http.post<any>(`${this.baseUrl}/api/customers`, 
      customer,
      { headers: this.buildHeaders().append('Content-Type', 'application/json') });
  }

  getProfiles(): Observable<DocumentProfile[]> {
    return this.http.get<DocumentProfile[]>(`${this.baseUrl}/api/customers`, {
      headers: this.buildHeaders(),
    });
  }

  getDocuments(): Observable<DocumentItem[]> {
    return this.http.get<DocumentItem[]>(`${this.baseUrl}/api/customers/current/documents`, {
      headers: this.buildHeaders(),
    });
  }

  private buildHeaders(): HttpHeaders {
    const authHeader = this.auth.getAuthorizationHeader();
    return new HttpHeaders(authHeader);
  }
}
