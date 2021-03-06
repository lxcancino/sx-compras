import { Injectable } from '@angular/core';
import { HttpClient, HttpParams, HttpHeaders } from '@angular/common/http';

import { Observable, throwError } from 'rxjs';
import { catchError, finalize } from 'rxjs/operators';

import { ConfigService } from '../../utils/config.service';
import { TdLoadingService } from '@covalent/core';

@Injectable({
  providedIn: 'root'
})
export class CfdiService {
  private apiUrl: string;

  constructor(
    private http: HttpClient,
    private config: ConfigService,
    private loadingService: TdLoadingService
  ) {
    this.apiUrl = config.buildApiUrl('cfdi');
  }

  fetchXml(id: string): Observable<any> {
    const url = `${this.apiUrl}/mostrarXml/${id}`;
    const headers = new HttpHeaders().set('Content-type', 'text/xml');
    return this.http
      .get(url, {
        headers: headers,
        responseType: 'blob'
      })
      .pipe(catchError((error: any) => throwError(error)));
  }

  mostrarXml(cfdiId: string) {
    this.fetchXml(cfdiId).subscribe(
      res => {
        const blob = new Blob([res], {
          type: 'text/xml'
        });
        const fileURL = window.URL.createObjectURL(blob);
        window.open(fileURL, '_blank');
      },
      error => console.error('Error descrgando XML de CFDI: ', cfdiId)
    );
  }

  email(cfdiId: string, target: string): Observable<any> {
    const url = `${this.apiUrl}/email/${cfdiId}`;
    const params = new HttpParams().set('target', target);
    this.loadingService.register();
    return this.http
      .get(url, {
        params
      })
      .pipe(
        finalize(() => this.loadingService.resolve()),
        catchError((error: any) => throwError(error))
      );
  }
}
