import { Component, OnInit } from '@angular/core';
import { FormGroup, FormBuilder, Validators } from '@angular/forms';

import { Store, select } from '@ngrx/store';
import * as fromStore from '../../store';
import { Observable } from 'rxjs';

@Component({
  selector: 'sx-login-page',
  templateUrl: './login-page.component.html'
})
export class LoginPageComponent implements OnInit {
  loading$: Observable<boolean>;
  error$: Observable<any>;
  form: FormGroup;
  constructor(
    private fb: FormBuilder,
    private store: Store<fromStore.AuthState>
  ) {}

  ngOnInit() {
    this.loading$ = this.store.pipe(select(fromStore.getAuthLoading));
    this.error$ = this.store.pipe(select(fromStore.getAuthError));

    this.form = this.fb.group({
      username: [null, [Validators.required, Validators.minLength(4)]],
      password: [null, [Validators.required, Validators.minLength(4)]]
    });
  }

  login() {
    if (this.form.valid) {
      this.store.dispatch(new fromStore.Login(this.form.value));
    }
  }
}
