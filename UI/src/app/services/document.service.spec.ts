import '@angular/compiler';
import { HttpClientTestingModule, HttpTestingController } from '@angular/common/http/testing';
import { TestBed } from '@angular/core/testing';
import { BrowserDynamicTestingModule, platformBrowserDynamicTesting } from '@angular/platform-browser-dynamic/testing';

TestBed.initTestEnvironment(BrowserDynamicTestingModule, platformBrowserDynamicTesting());

import { environment } from '../../environments/environment';
import { AuthService } from '../auth/auth.service';
import { DocumentService } from './document.service';

describe('DocumentService', () => {
  let service: DocumentService;
  let httpMock: HttpTestingController;

  beforeEach(() => {
    TestBed.configureTestingModule({
      imports: [HttpClientTestingModule],
      providers: [
        {
          provide: AuthService,
          useValue: {
            getAuthorizationHeader: () => ({ Authorization: 'Bearer demo-token' }),
          },
        },
      ],
    });

    service = TestBed.inject(DocumentService);
    httpMock = TestBed.inject(HttpTestingController);
  });

  afterEach(() => httpMock.verify());

  it('uploads a file with the bearer token header', () => {
    const formData = new FormData();
    formData.append('file', new Blob(['hello world'], { type: 'text/plain' }), 'sample.txt');
    formData.append('customerName', 'Acme');

    service.uploadDocument(formData).subscribe((documents) => {
      expect(documents).toEqual([
        { name: 'sample.txt', size: 11, uploadedAt: new Date('2024-01-01T00:00:00Z'), link: '/documents/sample.txt' },
      ]);
    });

    const req = httpMock.expectOne(`${environment.apiBaseUrl}/api/document/upload`);
    expect(req.request.method).toBe('POST');
    expect(req.request.headers.get('Authorization')).toBe('Bearer demo-token');
    req.flush([
      { name: 'sample.txt', size: 11, uploadedAt: new Date('2024-01-01T00:00:00Z'), link: '/documents/sample.txt' },
    ]);
  });

  it('sends an empty authorization header when no token is available', () => {
    const authService = TestBed.inject(AuthService) as { getAuthorizationHeader: () => Record<string, string> };
    authService.getAuthorizationHeader = () => ({ });

    const formData = new FormData();
    formData.append('file', new Blob(['x'], { type: 'text/plain' }), 'small.txt');

    service.uploadDocument(formData).subscribe();

    const req = httpMock.expectOne(`${environment.apiBaseUrl}/api/document/upload`);
    expect(req.request.headers.has('Authorization')).toBe(false);
    req.flush([]);
  });
});
