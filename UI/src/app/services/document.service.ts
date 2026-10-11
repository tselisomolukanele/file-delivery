import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';

import { AuthService } from '../auth/auth.service';

export interface DocumentProfile {
  pofile: string;
}

export interface DocumentItem {
  id: string;
  name: string;
  status: string;
}

@Injectable({ providedIn: 'root' })
export class DocumentService {
  private readonly http = inject(HttpClient);
  private readonly auth = inject(AuthService);
  private readonly baseUrl = 'http://localhost:8081/api';

  getProfiles(): Observable<DocumentProfile[]> {
    return this.http.get<DocumentProfile[]>(`${this.baseUrl}/profiles`, {
      headers: this.buildHeaders(),
    });
  }

  getDocuments(): Observable<DocumentItem[]> {
    return this.http.get<DocumentItem[]>(`${this.baseUrl}/documents`, {
      headers: this.buildHeaders(),
    });
  }

  private buildHeaders(): HttpHeaders {
    const authHeader = this.auth.getAuthorizationHeader();
    return new HttpHeaders(authHeader);
  }
}
