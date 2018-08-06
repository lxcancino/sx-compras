import { Injectable } from '@angular/core';
import { HttpClient, HttpParams, HttpHeaders } from '@angular/common/http';

import { Observable, throwError } from 'rxjs';
import { catchError } from 'rxjs/operators';

import { ConfigService } from 'app/utils/config.service';

import * as _ from 'lodash';
import { CuentaPorPagar } from '../model/cuentaPorPagar';
import { Periodo } from '../../_core/models/periodo';

@Injectable()
export class CuentaPorPagarService {
  private apiUrl: string;

  constructor(private http: HttpClient, private configService: ConfigService) {
    this.apiUrl = configService.buildApiUrl('cuentaPorPagar');
  }

  list(periodo: Periodo): Observable<CuentaPorPagar[]> {
    const data = periodo.toApiJSON();
    const params = new HttpParams()
      .set('fechaInicial', data.fechaInicial)
      .set('fechaFinal', data.fechaFinal);
    return this.http
      .get<CuentaPorPagar[]>(this.apiUrl, { params: params })
      .pipe(catchError((error: any) => throwError(error)));
  }

  pendientesDeAnalizar(proveedorId: string): Observable<CuentaPorPagar[]> {
    const url = `${this.apiUrl}/pendientesDeAnalisis/${proveedorId}`;
    return this.http
      .get<CuentaPorPagar[]>(url)
      .pipe(catchError((error: any) => throwError(error)));
  }

  update(factura: CuentaPorPagar): Observable<CuentaPorPagar> {
    const url = `${this.apiUrl}/${factura.id}`;
    return this.http
      .put<CuentaPorPagar>(url, factura)
      .pipe(catchError((error: any) => throwError(error)));
  }

  saldar(factura: CuentaPorPagar): Observable<CuentaPorPagar> {
    const url = `${this.apiUrl}/${factura.id}/saldar`;
    return this.http
      .put<CuentaPorPagar>(url, factura)
      .pipe(catchError((error: any) => throwError(error)));
  }

  get(id: string): Observable<CuentaPorPagar> {
    const url = `${this.apiUrl}/${id}`;
    return this.http
      .get<CuentaPorPagar>(url)
      .pipe(catchError((error: any) => throwError(error)));
  }
}
