import { Injectable } from '@angular/core';

import { map, switchMap, catchError } from 'rxjs/operators';
import { of } from 'rxjs';

import * as fromClases from '../actions/clases.actions';

import { Effect, Actions, ofType } from '@ngrx/effects';
import { ClasesService } from '../../services';

@Injectable()
export class ClasesEffects {
  constructor(private actions$: Actions, private service: ClasesService) {}

  @Effect()
  loadClases$ = this.actions$.pipe(
    ofType(fromClases.LOAD_CLASES),
    switchMap(() => {
      return this.service
        .list()
        .pipe(
          map(clases => new fromClases.LoadClasesSuccess(clases)),
          catchError(error => of(new fromClases.LoadClasesFail(error)))
        );
    })
  );

  @Effect()
  createClase$ = this.actions$.pipe(ofType(fromClases.CREATE_CLASE),
    map((action: fromClases.CreateClase) => action.payload),
    switchMap(clase => {
      return this.service
        .save(clase)
        .pipe(
          map(res => new fromClases.CreateClaseSuccess(res)),
          catchError(error => of(new fromClases.CreateClaseFail(error)))
        );
    })
  );

  @Effect()
  updateClase$ = this.actions$.pipe(ofType(fromClases.UPDATE_CLASE),
    map((action: fromClases.UpdateClase) => action.payload),
    switchMap(clase => {
      return this.service
        .update(clase)
        .pipe(
          map(res => new fromClases.UpdateClaseSuccess(res)),
          catchError(error => of(new fromClases.UpdateClaseFail(error)))
        );
    })
  );
}
