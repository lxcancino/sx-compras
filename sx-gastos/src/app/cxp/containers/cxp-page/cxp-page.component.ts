import { Component, OnInit } from '@angular/core';

import { TdMediaService } from '@covalent/core';

import { Store, select } from '@ngrx/store';
import * as fromStore from '../../store';

import { Observable } from 'rxjs';

@Component({
  selector: 'sx-cxp-page',
  templateUrl: './cxc-page.component.html',
  styleUrls: ['./cxp-page.component.scss']
})
export class CxpPageComponent implements OnInit {
  navmenu: Object[] = [
    {
      route: 'cfdis',
      title: 'CFDIs',
      description: 'Comprobantes fiscales',
      icon: 'account_balance_wallet'
    },
    {
      route: 'facturas',
      title: 'Facturas',
      description: 'Facturas (CFDIs)',
      icon: 'receipt'
    },
    {
      route: 'requisiciones',
      title: 'Requisiciones',
      description: 'Requisiciones',
      icon: 'gradient'
    },
    {
      route: 'pagos',
      title: 'Pagos ',
      description: 'Pagos registrados',
      icon: 'money_off'
    },
    {
      route: 'cheques',
      title: 'Cheques',
      description: 'Entrega de cheques',
      icon: 'account_balance_wallet'
    },
    {
      route: 'notas',
      title: 'Notas ',
      description: 'Notas de crédito',
      icon: 'description'
    }
  ];

  loading$: Observable<boolean>;

  constructor(
    public media: TdMediaService,
    private store: Store<fromStore.State>
  ) {}

  ngOnInit() {
    this.loading$ = this.store.pipe(select(fromStore.getNotasLoading));
  }
}
