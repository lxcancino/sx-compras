import { Injectable } from '@angular/core';
import { HttpClient, HttpParams, HttpHeaders } from '@angular/common/http';

import { Observable, of } from 'rxjs';
import { catchError } from 'rxjs/operators';

import { Cliente } from 'app/models';

import { CuentaPorCobrar } from 'app/cobranza/models';
import { ConfigService } from 'app/utils/config.service';

import * as _ from 'lodash';

@Injectable({
  providedIn: 'root'
})
export class SelectorCxcService {
  private apiUrl: string;

  constructor(private http: HttpClient, private config: ConfigService) {
    this.apiUrl = config.buildApiUrl('cuentasPorCobrar');
  }

  cuentasPorCobrar(
    cliente: Cliente,
    cartera: string
  ): Observable<CuentaPorCobrar[]> {
    const params = new HttpParams().set('cartera', cartera);
    const url = this.config.buildApiUrl(
      `cuentasPorCobrar/pendientes/${cliente.id}`
    );
    return this.http
      .get<CuentaPorCobrar[]>(url, { params })
      .pipe(catchError(err => of(err)));
  }
}
