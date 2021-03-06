import { ActionReducerMap, createFeatureSelector } from '@ngrx/store';

import * as fromAlcance from './alcance.reducer';
import * as fromCompras from './compra.reducer';
import * as fromProductosDisponibles from './productos-disponibles.reducer';

export interface State {
  compras: fromCompras.State;
  alcance: fromAlcance.State;
  disponibles: fromProductosDisponibles.State;
}

export const reducers: ActionReducerMap<State> = {
  compras: fromCompras.reducer,
  alcance: fromAlcance.reducer,
  disponibles: fromProductosDisponibles.reducer
};

export const getOrdenesState = createFeatureSelector<State>('ordenes');
