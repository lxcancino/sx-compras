import { Injectable } from '@angular/core';
import { CanActivate } from '@angular/router';

import { Store } from '@ngrx/store';
import { Observable, of } from 'rxjs';
import { tap, filter, take, switchMap, catchError } from 'rxjs/operators';

import * as fromStore from '../store';
import * as fromActions from '../store/actions/ficha.actions';

@Injectable()
export class FichasGuard implements CanActivate {
  constructor(private store: Store<fromStore.State>) {}

  canActivate(): Observable<boolean> {
    return this.checkStore().pipe(
      switchMap(() => of(true)),
      catchError(() => of(false))
    );
  }

  checkStore(): Observable<boolean> {
    return this.store.select(fromStore.getFichasLoaded).pipe(
      tap(loaded => {
        if (!loaded) {
          this.store.dispatch(new fromActions.LoadFichas());
        }
      }),
      filter(loaded => loaded), // Waiting for loaded
      take(1) // End the stream
    );
  }
}
