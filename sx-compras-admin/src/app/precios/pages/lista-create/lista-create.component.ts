import { Component, OnInit, ChangeDetectionStrategy } from '@angular/core';

import { Store, select } from '@ngrx/store';
import * as fromRoot from 'app/store';
import * as fromStore from '../../store';

import { Observable } from 'rxjs';

import { ListaDePreciosVenta } from '../../models';

@Component({
  selector: 'sx-lista-create',
  templateUrl: './lista-create.component.html',
  styleUrls: ['./lista-create.component.scss'],
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class ListaCreateComponent implements OnInit {
  loading$: Observable<boolean>;
  disponibles$: Observable<any[]>;

  constructor(private store: Store<fromStore.State>) {}

  ngOnInit() {
    this.loading$ = this.store.pipe(select(fromStore.selectListasLoading));
    this.disponibles$ = this.store.pipe(select(fromStore.selectDisponibles));
  }

  onBack() {
    this.store.dispatch(new fromRoot.Back());
  }

  onSave(lista: ListaDePreciosVenta) {
    // console.log('Lista: ', lista);
    this.store.dispatch(new fromStore.CreateLista({ lista }));
  }
}
