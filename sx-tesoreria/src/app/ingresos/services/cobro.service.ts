import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';

import { Observable, throwError } from 'rxjs';
import { catchError } from 'rxjs/operators';

import { ConfigService } from '../../utils/config.service';
import { Cobro, CobrosFilter } from '../models/cobro';

@Injectable()
export class CobroService {
  private apiUrl: string;

  constructor(private http: HttpClient, private config: ConfigService) {
    this.apiUrl = config.buildApiUrl('cxc/cobros');
  }

  get(id: string): Observable<Cobro> {
    const url = `${this.apiUrl}/${id}`;
    return this.http.get<Cobro>(url);
  }

  list(filter: CobrosFilter): Observable<Cobro[]> {
    let params = new HttpParams()
      .set('tipo', filter.tipo)
      .set('registros', filter.registros.toString())
      .set('fechaInicial', filter.fechaInicial.toISOString())
      .set('fechaFinal', filter.fechaFinal.toISOString());
    if (filter.nombre) {
      params = params.set('nombre', filter.nombre);
    }
    return this.http
      .get<Cobro[]>(this.apiUrl, { params: params })
      .pipe(catchError((error: any) => throwError(error)));
  }

  save(cobro: Cobro): Observable<Cobro> {
    return this.http
      .post<Cobro>(this.apiUrl, cobro)
      .pipe(catchError((error: any) => throwError(error)));
  }

  delete(cobro: Cobro) {
    const url = `${this.apiUrl}/${cobro.id}`;
    return this.http
      .delete<Cobro>(url)
      .pipe(catchError((error: any) => throwError(error)));
  }

  update(update: {
    id: string | number;
    changes: Partial<Cobro>;
  }): Observable<Cobro> {
    const url = `${this.apiUrl}/${update.id}`;
    return this.http
      .put<Cobro>(url, update.changes)
      .pipe(catchError((error: any) => throwError(error)));
  }

  registrarChequeDevuelto(fecha: Date, com: Cobro): Observable<Cobro> {
    const url = `${this.apiUrl}/registrarChequeDevuelto/${com.id}`;
    const params = new HttpParams().set('fecha', fecha.toISOString());
    return this.http
      .put<Cobro>(url, {}, { params: params })
      .pipe(catchError((error: any) => throwError(error)));
  }
}
