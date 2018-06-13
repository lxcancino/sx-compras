import { Injectable } from '@angular/core';
import { HttpClient, HttpParams, HttpHeaders } from '@angular/common/http';

import { Observable, throwError } from 'rxjs';
import { catchError } from 'rxjs/operators';

import { ConfigService } from 'app/utils/config.service';

import * as _ from 'lodash';
import { Analisis } from '../model/analisis';

@Injectable()
export class AnalisisService {
  private apiUrl: string;

  constructor(private http: HttpClient, private configService: ConfigService) {
    this.apiUrl = configService.buildApiUrl('analisisDeFactura');
  }

  list(filtro: any = {}): Observable<Analisis[]> {
    let params = new HttpParams();
    _.forIn(filtro, (value, key) => {
      params = params.set(key, value);
    });
    console.log('Params: ', filtro);
    return this.http
      .get<Analisis[]>(this.apiUrl, { params: params })
      .pipe(catchError((error: any) => throwError(error)));
  }

  get(id: string): Observable<Analisis> {
    const url = `${this.apiUrl}/${id}`;
    return this.http
      .get<Analisis>(url)
      .pipe(catchError((error: any) => throwError(error)));
  }

  save(analisis: Analisis): Observable<Analisis> {
    return this.http
      .post<Analisis>(this.apiUrl, analisis)
      .pipe(catchError((error: any) => throwError(error)));
  }

  update(analisis: Analisis): Observable<Analisis> {
    const url = `${this.apiUrl}/${analisis.id}`;
    return this.http
      .put<Analisis>(url, analisis)
      .pipe(catchError((error: any) => throwError(error)));
  }

  delete(id: string) {
    const url = `${this.apiUrl}/${id}`;
    return this.http
      .delete(url)
      .pipe(catchError((error: any) => throwError(error)));
  }
}
