import { Component, OnInit } from '@angular/core';

import { of as observableOf, Observable } from 'rxjs';
import { Store, select } from '@ngrx/store';
import * as fromAuth from 'app/auth/store';
import { AuthSession } from '../../../auth/models/authSession';
import { User } from 'app/auth/models/user';

@Component({
  selector: 'sx-home-page',
  templateUrl: './home-page.component.html',
  styles: []
})
export class HomePageComponent implements OnInit {
  header$: Observable<string>;
  application$: Observable<any>;
  session$: Observable<AuthSession>;
  user$: Observable<User>;
  api$: Observable<any>;

  constructor(private store: Store<fromAuth.AuthState>) {}

  ngOnInit() {
    this.header$ = observableOf('SX Gastos');
    this.application$ = observableOf({
      name: 'SX GASTOS',
      description: 'SIIPAPX sistema de control de gastos y adquisiciones',
      image: '/assets/images/logo_papelsa.jpg'
    });
    this.session$ = this.store.pipe(select(fromAuth.getSession));
    this.user$ = this.store.pipe(select(fromAuth.getUser));
    this.api$ = this.store.pipe(select(fromAuth.getApiInfo));
  }
}
