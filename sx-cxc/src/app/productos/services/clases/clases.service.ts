
import {throwError as observableThrowError,  Observable } from 'rxjs';
import { Injectable } from '@angular/core';
import { HttpClient, HttpParams, HttpHeaders } from '@angular/common/http';
import { map, switchMap, catchError } from 'rxjs/operators';

import * as _ from 'lodash';

import { ConfigService } from 'app/utils/config.service';
import { Clase } from '../../models/clase';

@Injectable()
export class ClasesService {
  private apiUrl: string;

  constructor(private http: HttpClient, private configService: ConfigService) {
    this.apiUrl = configService.buildApiUrl('clases');
  }

  list(): Observable<Clase[]> {
    return this.http
      .get<Clase[]>(this.apiUrl)
      .pipe(catchError((error: any) => observableThrowError(error)));
  }

  save(clase: Clase): Observable<Clase> {
    return this.http
      .post<Clase>(this.apiUrl, clase)
      .pipe(catchError(error => observableThrowError(error)));
  }

  update(clase: Clase): Observable<Clase> {
    const url = `${this.apiUrl}/${clase.id}`;
    return this.http
      .put<Clase>(url, clase)
      .pipe(catchError(error => observableThrowError(error)));
  }

  delete(clase: Clase) {
    const url = `${this.apiUrl}/${clase.id}`;
    return this.http
      .delete(url)
      .pipe(catchError(error => observableThrowError(error)));
  }
}
