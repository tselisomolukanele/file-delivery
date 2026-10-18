import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';

import { environment } from '../../environments/environment';
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
  private readonly baseUrl = environment.apiBaseUrl;

  uploadDocument(formData: FormData): Observable<DocumentItem[]> {
    return this.http.post<DocumentItem[]>(`${this.baseUrl}/api/document/upload`, 
      formData,
      { headers: this.buildHeaders()});
  }

  private buildHeaders(): HttpHeaders {
    const authHeader = this.auth.getAuthorizationHeader();
    return new HttpHeaders(authHeader);
  }
}
