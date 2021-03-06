import { Component, OnInit, OnDestroy } from '@angular/core';

import { Store, select } from '@ngrx/store';
import * as fromStore from '../../store';
import * as fromActions from '../../store/actions/compra.actions';

import { Observable } from 'rxjs';

import { Compra } from '../../models/compra';

import { TdDialogService } from '@covalent/core';

import { ProveedorProducto } from '../../../proveedores/models/proveedorProducto';
import { CompraUiService } from './compra-ui.service';

@Component({
  selector: 'sx-compra',
  template: `
    <div>
      <ng-template
        tdLoading
        [tdLoadingUntil]="!(loading$ | async)"
        tdLoadingStrategy="overlay"
      >
        <sx-compra-form
          [compra]="compra$ | async"
          [productos]="productos$ | async"
          (save)="onSave($event)"
          (delete)="onDelete($event)"
        >
          <ng-container *ngIf="compra$ | async as compra">
            <sx-email-compra [compra]="compra"></sx-email-compra>
            <sx-compra-print [compra]="compra"></sx-compra-print>
          </ng-container>
        </sx-compra-form>
      </ng-template>
    </div>
  `,
  providers: [CompraUiService]
})
export class CompraComponent implements OnInit, OnDestroy {
  compra$: Observable<Compra>;
  productos$: Observable<ProveedorProducto[]>;
  loading$: Observable<boolean>;
  constructor(
    private store: Store<fromStore.State>,
    private dialogService: TdDialogService
  ) {}

  ngOnInit() {
    this.loading$ = this.store.pipe(select(fromStore.getComprasLoading));
    this.compra$ = this.store.pipe(select(fromStore.getSelectedCompra));
    this.productos$ = this.store.pipe(
      select(fromStore.getAllProductosDisponibles)
    );
    this.store.dispatch(new fromStore.LoadProductosDisponibles());
  }

  ngOnDestroy() {
    this.store.dispatch(new fromStore.ClearProductosDisponibles());
  }

  onSave(event: Compra) {
    if (!event.id) {
      this.store.dispatch(new fromActions.AddCompra(event));
    } else {
      this.store.dispatch(new fromActions.UpdateCompra(event));
    }
  }

  onDelete(event: Compra) {
    this.store.dispatch(new fromActions.DeleteCompra(event));
  }
  onCerrar(event: Compra) {
    console.log('Cerrar: ', event);
  }

  onDepurar(event: Compra) {
    this.store.dispatch(new fromActions.DepurarCompra(event));
  }

  getPrintUrl(event: Compra) {
    return `compras/print/${event.id}`;
  }
}
