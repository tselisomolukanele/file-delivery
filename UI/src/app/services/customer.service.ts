import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';

import { environment } from '../../environments/environment';
import { AuthService } from '../auth/auth.service';
import { DocumentItem, DocumentProfile } from './document.service';

@Injectable({ providedIn: 'root' })
export class CustomerService {
  private readonly http = inject(HttpClient);
  private readonly auth = inject(AuthService);
  private readonly baseUrl = environment.apiBaseUrl;

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

  download(documentId: number): Observable<Blob> {
    return this.http.get(`${this.baseUrl}/api/customers/current/document/download/${documentId}`, { 
      headers: this.buildHeaders(),
      responseType: 'blob' 
    });
  }

  getOTP(): Observable<string> {
    return this.http.post<string>(`${this.baseUrl}/api/customers/current/otp`,
      null, 
      { headers: this.buildHeaders() });
  }

  private buildHeaders(): HttpHeaders {
    const authHeader = this.auth.getAuthorizationHeader();
    return new HttpHeaders(authHeader);
  }
}
