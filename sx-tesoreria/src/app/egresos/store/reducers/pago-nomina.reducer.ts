import { EntityState, EntityAdapter, createEntityAdapter } from '@ngrx/entity';

import { PagoDeNomina, PagosDeNominaFilter } from '../../models';

import {
  PagoDeNominaActions,
  PagoDeNominaActionTypes
} from '../actions/pago-nomina.actions';
import { Periodo } from 'app/_core/models/periodo';

export function createPagosDeNominaFilter(dias = 20): PagosDeNominaFilter {
  const { fechaInicial, fechaFinal } = Periodo.fromNow(dias);
  const registros = 20;
  return {
    fechaInicial,
    fechaFinal,
    registros,
    pendientes: true
  };
}

export interface State extends EntityState<PagoDeNomina> {
  loading: boolean;
  loaded: boolean;
  filter: PagosDeNominaFilter;
  term: string;
}

export const adapter: EntityAdapter<PagoDeNomina> = createEntityAdapter<
  PagoDeNomina
>();

export const initialState: State = adapter.getInitialState({
  loading: false,
  loaded: false,
  filter: createPagosDeNominaFilter(30),
  term: ''
});

export function reducer(
  state = initialState,
  action: PagoDeNominaActions
): State {
  switch (action.type) {
    case PagoDeNominaActionTypes.SetPagoDeNominasFilter: {
      const filter = action.payload.filter;
      return {
        ...state,
        filter
      };
    }

    case PagoDeNominaActionTypes.GenerarChequeDePago:
    case PagoDeNominaActionTypes.DeletePagoDeNomina:
    case PagoDeNominaActionTypes.ImportarPagosDeNomina:
    case PagoDeNominaActionTypes.PagarNomina:
    case PagoDeNominaActionTypes.LoadPagoDeNominas: {
      return {
        ...state,
        loading: true
      };
    }

    case PagoDeNominaActionTypes.PagoDeNominaError: {
      return {
        ...state,
        loading: false
      };
    }

    case PagoDeNominaActionTypes.LoadPagoDeNominasSuccess: {
      return adapter.addAll(action.payload.pagos, {
        ...state,
        loading: false,
        loaded: true
      });
    }

    case PagoDeNominaActionTypes.ImportarPagosDeNominaSuccess: {
      const pagos = action.payload.pagos;
      return adapter.addMany(pagos, {
        ...state,
        loading: false
      });
    }

    case PagoDeNominaActionTypes.DeletePagoDeNominaSuccess: {
      return adapter.removeOne(action.payload.pago.id, {
        ...state,
        loading: false
      });
    }

    case PagoDeNominaActionTypes.GenerarChequeDePagoSuccess:
    case PagoDeNominaActionTypes.PagarNominaSuccess: {
      return adapter.upsertOne(action.payload.pago, {
        ...state,
        loading: false
      });
    }

    case PagoDeNominaActionTypes.UpsertPagoDeNomina: {
      return adapter.upsertOne(action.payload.pago, {
        ...state
      });
    }

    default: {
      return state;
    }
  }
}

export const {
  selectIds,
  selectEntities,
  selectAll,
  selectTotal
} = adapter.getSelectors();

export const getPagoDeNominasLoading = (state: State) => state.loading;
export const getPagoDeNominasLoaded = (state: State) => state.loaded;
export const getPagoDeNominasFilter = (state: State) => state.filter;
export const getPagoDeNominasSearchTerm = (state: State) => state.term;
