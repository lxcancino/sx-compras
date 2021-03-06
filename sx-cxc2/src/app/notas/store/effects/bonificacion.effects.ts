import { Injectable } from '@angular/core';

import { Actions, Effect, ofType } from '@ngrx/effects';

import { Store, select } from '@ngrx/store';
import * as fromRoot from 'app/store';
import * as fromStore from '../../store';

import { of } from 'rxjs';
import { catchError, map, switchMap, take } from 'rxjs/operators';

import * as fromActions from '../actions/bonificacion.actions';
import { BonificacionActionTypes } from '../actions/bonificacion.actions';
import { NotaDeCreditoService } from '../../services/nota-de-credito.service';
import { Bonificacion } from 'app/cobranza/models';

@Injectable()
export class BonificacionEffects {
  @Effect()
  loadNotas$ = this.actions$.pipe(
    ofType<fromActions.LoadBonificaciones>(
      BonificacionActionTypes.LoadBonificaciones
    ),
    map(action => action.payload),
    switchMap(payload =>
      this.service.list(payload.cartera.clave, payload.filter).pipe(
        map(
          (bonificaciones: Bonificacion[]) =>
            new fromActions.LoadBonificacionesSuccess({ bonificaciones })
        ),
        catchError((response: any) =>
          of(new fromActions.LoadBonificacionesFail({ response }))
        )
      )
    )
  );

  @Effect()
  createNota$ = this.actions$.pipe(
    ofType<fromActions.CreateBonificacion>(
      BonificacionActionTypes.CreateBonificacion
    ),
    map(action => action.payload.bonificacion),
    switchMap(nota =>
      this.service.save(nota).pipe(
        map(
          (bonificacion: Bonificacion) =>
            new fromActions.CreateBonificacionSuccess({ bonificacion })
        ),
        catchError(response =>
          of(new fromActions.CreateBonificacionFail({ response }))
        )
      )
    )
  );

  @Effect()
  createSuccess$ = this.actions$.pipe(
    ofType<fromActions.CreateBonificacionSuccess>(
      BonificacionActionTypes.CreateBonificacionSuccess
    ),
    map(action => action.payload.bonificacion),
    map(
      bonificacion =>
        new fromRoot.Go({
          path: ['credito/notas/bonificaciones/edit', bonificacion.id]
        })
    )
  );

  @Effect()
  updateNota$ = this.actions$.pipe(
    ofType<fromActions.UpdateBonificacion>(
      BonificacionActionTypes.UpdateBonificacion
    ),
    map(action => action.payload.update),
    switchMap(nota =>
      this.service.update(nota).pipe(
        map(
          (bonificacion: Bonificacion) =>
            new fromActions.UpdateBonificacionSuccess({ bonificacion })
        ),
        catchError(response =>
          of(new fromActions.UpdateBonificacionFail({ response }))
        )
      )
    )
  );

  @Effect()
  deleteNota$ = this.actions$.pipe(
    ofType<fromActions.DeleteBonificacion>(
      BonificacionActionTypes.DeleteBonificacion
    ),
    map(action => action.payload.bonificacion),
    switchMap(nota =>
      this.service.delete(nota.id).pipe(
        map(
          res =>
            new fromActions.DeleteBonificacionSuccess({ bonificacion: nota })
        ),
        catchError(response =>
          of(new fromActions.DeleteBonificacionFail({ response }))
        )
      )
    )
  );

  @Effect()
  deleteSuccess$ = this.actions$.pipe(
    ofType<fromActions.DeleteBonificacionSuccess>(
      BonificacionActionTypes.DeleteBonificacionSuccess
    ),
    map(action => action.payload.bonificacion),
    map(
      () =>
        new fromRoot.Go({
          path: ['credito/notas/bonificaciones']
        })
    )
  );

  @Effect()
  generarCfdi$ = this.actions$.pipe(
    ofType<fromActions.GenerarBonificacionCfdi>(
      BonificacionActionTypes.GenerarBonificacionCfdi
    ),
    map(action => action.payload.notaId),
    switchMap(notaId =>
      this.service.gemerarCfdi(notaId).pipe(
        map(
          (bonificacion: Bonificacion) =>
            new fromActions.UpdateBonificacionSuccess({ bonificacion })
        ),
        catchError(response =>
          of(new fromActions.UpdateBonificacionFail({ response }))
        )
      )
    )
  );

  @Effect()
  cancelarCfdi$ = this.actions$.pipe(
    ofType<fromActions.CancelarBonificacionCfdi>(
      BonificacionActionTypes.CancelarBonificacionCfdi
    ),
    map(action => action.payload.notaId),
    switchMap(notaId =>
      this.service.gemerarCfdi(notaId).pipe(
        map(
          (bonificacion: Bonificacion) =>
            new fromActions.UpdateBonificacionSuccess({ bonificacion })
        ),
        catchError(response =>
          of(new fromActions.UpdateBonificacionFail({ response }))
        )
      )
    )
  );

  @Effect()
  aplicar$ = this.actions$.pipe(
    ofType<fromActions.AplicarBonificacion>(
      BonificacionActionTypes.AplicarBonificacion
    ),
    map(action => action.payload.notaId),
    switchMap(notaId =>
      this.service.aplicar(notaId).pipe(
        map(
          (bonificacion: Bonificacion) =>
            new fromActions.UpdateBonificacionSuccess({ bonificacion })
        ),
        catchError(response =>
          of(new fromActions.UpdateBonificacionFail({ response }))
        )
      )
    )
  );

  @Effect()
  fail$ = this.actions$.pipe(
    ofType<
      | fromActions.LoadBonificacionesFail
      | fromActions.CreateBonificacionFail
      | fromActions.UpdateBonificacionFail
      | fromActions.DeleteBonificacionFail
    >(
      BonificacionActionTypes.LoadBonificacionesFail,
      BonificacionActionTypes.CreateBonificacionFail,
      BonificacionActionTypes.UpdateBonificacionFail,
      BonificacionActionTypes.DeleteBonificacionFail
    ),
    map(action => action.payload.response),
    map(response => new fromRoot.GlobalHttpError({ response }))
  );

  constructor(
    private actions$: Actions<fromActions.BonificacionActions>,
    private service: NotaDeCreditoService,
    private store: Store<fromStore.State>
  ) {}
}
