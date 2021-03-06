import { Injectable } from '@angular/core';
import { Actions, Effect, ofType } from '@ngrx/effects';

import { Store, select } from '@ngrx/store';
import * as fromRoot from 'app/store';
import * as fromStore from '../../store';
import { getComprobantesFilter } from '../../store/selectors/cfdis.selectors';

import { of } from 'rxjs';
import { map, switchMap, tap, catchError, take } from 'rxjs/operators';

import { ComprobanteActionTypes } from '../actions/cfdi.actions';
import * as fromActions from '../actions/cfdi.actions';
import { ComprobanteFiscalService } from '../../services';

import { MatSnackBar } from '@angular/material';
import { ReportService } from '../../../reportes/services/report.service';

@Injectable()
export class CfdiEffects {
  constructor(
    private actions$: Actions,
    private service: ComprobanteFiscalService,
    private store: Store<fromStore.State>,
    private reportService: ReportService,
    private snackBar: MatSnackBar
  ) {}

  @Effect()
  loadCfdis$ = this.actions$.pipe(
    ofType(ComprobanteActionTypes.LoadComprobantes),
    switchMap(() => {
      return this.store.pipe(
        select(getComprobantesFilter),
        take(1)
      );
    }),
    switchMap(filter => {
      return this.service.list(filter).pipe(
        map(res => new fromActions.LoadComprobantesSuccess(res)),
        catchError(error => of(new fromActions.LoadComprobantesFail(error)))
      );
    })
  );

  @Effect()
  updateComprobante$ = this.actions$.pipe(
    ofType<fromActions.UpdateComprobante>(
      ComprobanteActionTypes.UpdateComprobante
    ),
    map(action => action.payload),
    switchMap(cfdi => {
      return this.service.update(cfdi).pipe(
        map(res => new fromActions.UpdateComprobanteSuccess(res)),
        catchError(error => of(new fromActions.UpdateComprobanteFail(error)))
      );
    })
  );

  @Effect()
  updateSuccess$ = this.actions$.pipe(
    ofType<fromActions.UpdateComprobanteSuccess>(
      ComprobanteActionTypes.UpdateComprobanteSuccess
    ),
    map(action => action.payload),
    tap(comprobante =>
      this.snackBar.open(
        `Comprobante ${comprobante.uuid} actualizada `,
        'Cerrar',
        {
          duration: 5000
        }
      )
    ),
    map(comprobante => new fromRoot.Go({ path: ['cxp/cfdis'] }))
  );

  @Effect({ dispatch: false })
  imprimirCfdi$ = this.actions$.pipe(
    ofType<fromActions.ImprimirComprobante>(
      ComprobanteActionTypes.ImprimirComprobante
    ),
    map(action => action.payload),
    tap(cfdi => this.reportService.runReport(`comprobanteFiscal/pdf/${cfdi}`))
  );

  @Effect({ dispatch: false })
  xmlCfdi$ = this.actions$.pipe(
    ofType<fromActions.MostrarXmlComprobante>(
      ComprobanteActionTypes.MostrarXmlDelComprobante
    ),
    map(action => action.payload.cfdi),
    tap(cfdi => this.service.mostrarXml(cfdi))
  );
}
