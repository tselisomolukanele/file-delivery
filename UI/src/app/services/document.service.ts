import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';

import { AuthService } from '../auth/auth.service';

export interface DocumentProfile {
  name: string;
}

export interface DocumentItem {
  name: string;
  link: string;
}

@Injectable({ providedIn: 'root' })
export class DocumentService {
  private readonly http = inject(HttpClient);
  private readonly auth = inject(AuthService);
  private readonly baseUrl = 'http://localhost:8081';

  uploadDocument(formData: FormData): Observable<DocumentItem[]> {
    return this.http.post<DocumentItem[]>(`${this.baseUrl}/document/upload`, 
      formData,
      { headers: this.buildHeaders()});
  }

  private buildHeaders(): HttpHeaders {
    const authHeader = this.auth.getAuthorizationHeader();
    return new HttpHeaders(authHeader);
  }
}
