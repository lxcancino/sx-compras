import {
  Component,
  OnInit,
  OnDestroy,
  ChangeDetectionStrategy
} from '@angular/core';

import { Store, select } from '@ngrx/store';
import * as fromRoot from 'app/store';
import * as fromStore from '../../store';
import * as fromActions from '../../store/actions/compra.actions';

import { Observable, of } from 'rxjs';
import { map } from 'rxjs/operators';

import { Compra, ComprasFilter } from '../../models/compra';

import * as _ from 'lodash';
import { CompraDet } from '../../models/compraDet';

@Component({
  selector: 'sx-compras',
  changeDetection: ChangeDetectionStrategy.OnPush,
  templateUrl: './compras.component.html',
  styleUrls: ['./compras.component.scss']
})
export class ComprasComponent implements OnInit, OnDestroy {
  compras$: Observable<Compra[]>;

  comprasPorSucursal$: Observable<any>;
  sucursales$: Observable<string[]>;

  filter$: Observable<ComprasFilter>;
  search$: Observable<string>;

  selected$: Observable<String[]>;
  partidas$: Observable<CompraDet[]>;

  tabIndex = 2;
  private _storageKey = 'sx-compras.ordenes';
  constructor(private store: Store<fromStore.State>) {}

  ngOnInit() {
    this.compras$ = this.store.pipe(select(fromStore.getAllCompras));

    this.comprasPorSucursal$ = this.store.pipe(
      select(fromStore.getComprasPorSucursalPendientes)
    );
    this.sucursales$ = this.comprasPorSucursal$.pipe(map(res => _.keys(res)));

    this.filter$ = this.store.pipe(select(fromStore.getComprasFilter));
    this.search$ = this.store.pipe(select(fromStore.getComprasSearchTerm));

    // Tab Idx
    const _tabIdx = localStorage.getItem(this._storageKey + '.tabIndex');
    this.tabIndex = parseFloat(_tabIdx);

    this.partidas$ = of([]); // this.store.pipe(select(fromStore.getSelectedPartidas));
    const lastSearch = localStorage.getItem(this._storageKey + '.filter');

    if (lastSearch) {
      this.onSearch(lastSearch);
    }

    this.selected$ = of([]); // this.store.pipe(select(fromStore.getSelectedComprasIds));
  }

  ngOnDestroy() {
    localStorage.setItem(
      this._storageKey + '.tabIndex',
      this.tabIndex.toString()
    );
  }

  onSearch(term: string) {
    this.store.dispatch(new fromActions.SetComprasSearchTerm({ term }));
  }

  onFilterChange(filter: ComprasFilter) {
    this.store.dispatch(new fromActions.SetComprasFilter({ filter }));
  }

  getSucursales(object): string[] {
    return _.keys(object);
  }

  onSelect(event: Compra) {
    this.onEdit(event);
  }

  onEdit(event: Compra) {
    this.store.dispatch(
      new fromRoot.Go({ path: ['ordenes/compras', event.id] })
    );
  }
}
