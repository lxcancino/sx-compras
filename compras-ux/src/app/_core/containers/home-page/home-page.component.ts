import { Component, OnInit } from '@angular/core';

import { of as observableOf, Observable } from 'rxjs';
import { Store, select } from '@ngrx/store';
import * as fromAuth from 'app/auth/store';
import { AuthSession } from '../../../auth/models/authSession';

@Component({
  selector: 'sx-home-page',
  templateUrl: './home-page.component.html',
  styles: []
})
export class HomePageComponent implements OnInit {
  header$: Observable<string>;
  application$: Observable<any>;
  session$: Observable<AuthSession>;

  constructor(private store: Store<fromAuth.AuthState>) {}

  ngOnInit() {
    this.header$ = observableOf('SX-CXP-COMPRAS');
    this.application$ = observableOf({
      name: 'SX COMPRAS CXP',
      descripcion: 'SIIPAPX cuenta por pagar para compras de materia prima',
      image: '/assets/images/logo_papelsa.jpg'
    });
    this.session$ = this.store.pipe(select(fromAuth.getSession));
  }
}
