import { TestBed } from '@angular/core/testing';
import { HttpClientTestingModule, HttpTestingController } from '@angular/common/http/testing';

import { environment } from '../../environments/environment';
import { AuthService } from '../auth/auth.service';
import { DocumentService } from './document.service';

describe('DocumentService', () => {
  let service: DocumentService;
  let httpMock: HttpTestingController;

  beforeEach(() => {
    const authServiceMock = {
      getAccessToken: () => 'demo-token',
      getAuthorizationHeader: () => ({ Authorization: 'Bearer demo-token' }),
    };

    TestBed.configureTestingModule({
      imports: [HttpClientTestingModule],
      providers: [{ provide: AuthService, useValue: authServiceMock }],
    });

    service = TestBed.inject(DocumentService);
    httpMock = TestBed.inject(HttpTestingController);
  });

  afterEach(() => httpMock.verify());

  it('loads the document profile from the backend', () => {
    service.getProfile().subscribe((profile) => {
      expect(profile.username).toBe('alice');
    });

    const req = httpMock.expectOne(`${environment.apiBaseUrl}/api/profile`);
    expect(req.request.method).toBe('GET');
    expect(req.request.headers.get('Authorization')).toBe('Bearer demo-token');
    req.flush({ username: 'alice', issuer: `${environment.authBaseUrl}/realms/document-service`, roles: ['user'] });
  });
});
