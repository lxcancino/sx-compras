import { Component, OnInit } from '@angular/core';
import { MatDialog } from '@angular/material';

import { Store, select } from '@ngrx/store';
import * as fromRoot from 'app/store';
import * as fromStore from '../../store';
import * as fromActions from '../../store/actions/listasDePrecios.actions';

import { Observable } from 'rxjs';

import { ListaDePreciosProveedor } from '../../models/listaDePreciosProveedor';

import { TdDialogService } from '@covalent/core';
import { ReportService } from '../../../reportes/services/report.service';
import { FechaDialogComponent } from '../../../_shared/components';

@Component({
  selector: 'sx-proveedor-lista-edit',
  template: `
    <div>
      <sx-proveedor-lista-form [listaDePrecios]="lista$ | async" (save)="onSave($event)"
        (cancel)="onCancel()"
        (aplicar)="onAplicar($event)"
        (actualizar)="onActualizar($event)"
        (actualizarCompras)="onActualizarCompras($event)"
        (print)="onPrint($event)"
        (delete)="onDelete($event)">
        </sx-proveedor-lista-form>
    </div>
  `
})
export class ProveedorListaEditComponent implements OnInit {
  lista$: Observable<ListaDePreciosProveedor>;
  constructor(
    private store: Store<fromStore.ProveedoresState>,
    private dialogService: TdDialogService,
    private reportService: ReportService,
    private dialog: MatDialog
  ) {}

  ngOnInit() {
    this.lista$ = this.store.pipe(select(fromStore.getSelectedLista));
  }

  onSave(event: ListaDePreciosProveedor) {
    this.store.dispatch(new fromActions.UpdateListaDePreciosProveedor(event));
  }

  onAplicar(event: ListaDePreciosProveedor) {
    this.dialogService
      .openConfirm({
        title: `Aplicar lista de precios ${event.id}`,
        message: 'Los precios seran replicados a las sucursales',
        acceptButton: 'Aplicar',
        cancelButton: 'Cancelar'
      })
      .afterClosed()
      .subscribe(res => {
        if (res) {
          this.store.dispatch(new fromActions.AplicarListaProveedor(event));
        }
      });
  }

  onActualizar(event: ListaDePreciosProveedor) {
    this.dialogService
      .openConfirm({
        title: `Actualizar lista de precios ${event.id}`,
        message: 'Agregar productos faltantes',
        acceptButton: 'Actualizar',
        cancelButton: 'Cancelar'
      })
      .afterClosed()
      .subscribe(res => {
        if (res) {
          this.store.dispatch(
            new fromActions.ActualizarProductosDeLista(event)
          );
        }
      });
  }

  onActualizarCompras(event: ListaDePreciosProveedor) {
    this.dialog
      .open(FechaDialogComponent, {
        data: {
          fecha: event.fechaInicial,
          title: 'Actualizar compras a partir de: '
        }
      })
      .afterClosed()
      .subscribe(fecha => {
        if (fecha) {
          this.store.dispatch(
            new fromActions.ActualizarComprasConLista({
              lista: event,
              fecha: fecha
            })
          );
        }
      });
  }

  onDelete(event: ListaDePreciosProveedor) {
    this.dialogService
      .openConfirm({
        title: `Eliminar lista de precios ${event.id}`,
        message: 'Folio:  ' + event.id,
        acceptButton: 'Elimiar',
        cancelButton: 'Cancelar'
      })
      .afterClosed()
      .subscribe(res => {
        if (res) {
          this.store.dispatch(
            new fromActions.DeleteListaDePreciosProveedor(event)
          );
        }
      });
  }

  onCancel() {
    this.store.dispatch(new fromRoot.Back());
  }

  onPrint(event: ListaDePreciosProveedor) {
    const url = `listaDePreciosProveedor/print/${event.id}`;
    this.dialogService
      .openConfirm({
        message: 'Con descuentos ?',
        title: 'Imprmir lista de precios',
        acceptButton: 'SI',
        cancelButton: 'NO'
      })
      .afterClosed()
      .subscribe(res => {
        console.log('Imprimir: ', event);
        if (res) {
          const params = { descuentos: true };
          this.reportService.runReport(url, params);
        } else {
          this.reportService.runReport(url);
        }
      });
  }

  
}
