import { Injectable } from '@angular/core';
import { HttpClient, HttpParams, HttpHeaders } from '@angular/common/http';

import { Observable, throwError } from 'rxjs';
import { catchError } from 'rxjs/operators';

import { ConfigService } from '../../utils/config.service';

import { Compra, ComprasFilter } from '../models/compra';

import { ProveedorProducto } from '../../proveedores/models/proveedorProducto';

@Injectable()
export class ComprasService {
  private apiUrl: string;

  constructor(private http: HttpClient, private configService: ConfigService) {
    this.apiUrl = configService.buildApiUrl('compras');
  }

  list(filter: ComprasFilter): Observable<Compra[]> {
    let params = new HttpParams();
    if (!filter.pendientes) {
      params = params
        .set('registros', filter.registros.toString())
        .set('fechaInicial', filter.fechaInicial.toISOString())
        .set('fechaFinal', filter.fechaFinal.toISOString());
    } else {
      params = params.set('pendientes', filter.pendientes.toString());
    }
    if (filter.proveedor) {
      params = params.set('proveedor', filter.proveedor.id);
    }
    if (filter.folio) {
      params = params.set('folio', filter.folio.toString());
    }
    return this.http.get<Compra[]>(this.apiUrl, { params: params });
  }

  get(id: string): Observable<Compra> {
    const url = `${this.apiUrl}/${id}`;
    return this.http.get<Compra>(url);
  }

  save(compra: Compra): Observable<Compra> {
    return this.http.post<Compra>(this.apiUrl, compra);
  }

  update(compra: Compra): Observable<Compra> {
    const url = `${this.apiUrl}/${compra.id}`;
    return this.http
      .put<Compra>(url, compra)
      .pipe(catchError((error: any) => throwError(error)));
  }

  cerrar(compra: Compra): Observable<Compra> {
    const url = this.configService.buildApiUrl('compras/cerrar/' + compra.id);
    return this.http.put<Compra>(url, {});
  }

  depurar(compra: Compra): Observable<Compra> {
    const url = this.configService.buildApiUrl('compras/depurar/' + compra.id);
    return this.http.put<Compra>(url, {});
  }

  delete(id: string) {
    const url = `${this.apiUrl}/${id}`;
    return this.http
      .delete(url)
      .pipe(catchError((error: any) => throwError(error)));
  }

  getProductosDisponibles(compra: Compra): Observable<ProveedorProducto[]> {
    const params = new HttpParams().set('moneda', compra.moneda);
    const url = `${this.configService.buildApiUrl('proveedores')}/${
      compra.proveedor.id
    }/productos`;
    return this.http.get<ProveedorProducto[]>(url, { params: params });
  }

  print(compra: Compra) {
    const endpoint = `compras/print/${compra.id}`;
    const url = this.configService.buildApiUrl(endpoint);
    const headers = new HttpHeaders().set('Content-type', 'application/pdf');
    return this.http.get(url, {
      headers: headers,
      responseType: 'blob'
    });
  }
}
