import { Injectable } from '@angular/core';

import { Effect, Actions, ofType } from '@ngrx/effects';
import { switchMap, map, catchError } from 'rxjs/operators';
import { of } from 'rxjs';

import * as fromMarcas from '../actions/marcas.actions';

import { MarcasService } from '../../services';

@Injectable()
export class MarcasEffects {
  constructor(private service: MarcasService, private actions$: Actions) {}

  @Effect()
  loadMarcas$ = this.actions$.pipe(
    ofType(fromMarcas.LOAD_MARCAS),
    switchMap(() => {
      return this.service.list().pipe(
        map(marcas => new fromMarcas.LoadMarcasSuccess(marcas)),
        catchError((error: any) => of(new fromMarcas.LoadMarcasFail(error)))
      );
    })
  );

  @Effect()
  createMarca$ = this.actions$.pipe(
    ofType(fromMarcas.CREATE_MARCA),
    map((action: fromMarcas.CreateMarca) => action.payload),
    switchMap(marca => {
      return this.service.save(marca).pipe(
        map(newMarca => new fromMarcas.CreateMarcaSuccess(newMarca)),
        catchError(error => of(new fromMarcas.CreateMarcaFail(error)))
      );
    })
  );

  @Effect()
  updateMarca$ = this.actions$.pipe(
    ofType(fromMarcas.UPDATE_MARCA),
    map((action: fromMarcas.UpdateMarca) => action.payload),
    switchMap(marca => {
      return this.service.update(marca).pipe(
        map(res => new fromMarcas.UpdateMarcaSuccess(res)),
        catchError(error => of(new fromMarcas.UpdateMarcaFail(error)))
      );
    })
  );

  @Effect()
  removeMarca$ = this.actions$.pipe(
    ofType(fromMarcas.REMOVE_MARCA),
    map((action: fromMarcas.RemoveMarca) => action.payload),
    switchMap(marca => {
      return this.service.delete(marca.id).pipe(
        map(() => new fromMarcas.RemoveMarcaSuccess(marca)),
        catchError(error => of(new fromMarcas.RemoveMarcaFail(error)))
      );
    })
  );
}
