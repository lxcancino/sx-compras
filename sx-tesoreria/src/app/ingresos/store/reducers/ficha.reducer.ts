import { EntityState, EntityAdapter, createEntityAdapter } from '@ngrx/entity';

import { Ficha, FichaFilter, buildFichasFilter } from '../../models';
import { FichaActions, FichaActionTypes } from '../actions/ficha.actions';

import * as moment from 'moment';

export interface State extends EntityState<Ficha> {
  loading: boolean;
  loaded: boolean;
  filter: FichaFilter;
}

export function comparLastUpdate(row1: Ficha, row2: Ficha): number {
  const d1 = moment(row1.modificado);
  const d2 = moment(row2.modificado);
  return d1.isSameOrBefore(d2) ? 1 : -1;
}

export const adapter: EntityAdapter<Ficha> = createEntityAdapter<Ficha>({
  sortComparer: comparLastUpdate
});

export const initialState: State = adapter.getInitialState({
  loading: false,
  loaded: false,
  filter: buildFichasFilter()
});

export function reducer(state = initialState, action: FichaActions): State {
  switch (action.type) {
    case FichaActionTypes.SetFichasFilter: {
      const filter = action.payload.filter;
      return {
        ...state,
        filter
      };
    }
    case FichaActionTypes.RegistrarIngreso:
    case FichaActionTypes.DeleteFicha:
    case FichaActionTypes.GenerateFichas:
    case FichaActionTypes.LoadFichas: {
      return {
        ...state,
        loading: true
      };
    }

    case FichaActionTypes.FichaError: {
      return {
        ...state,
        loading: false
      };
    }
    case FichaActionTypes.LoadFichasSuccess: {
      return adapter.addAll(action.payload.fichas, {
        ...state,
        loading: false,
        loaded: true
      });
    }

    case FichaActionTypes.GenerateFichasSuccess: {
      return adapter.addMany(action.payload.fichas, {
        ...state,
        loading: false
      });
    }

    case FichaActionTypes.DeleteFichaSuccess: {
      return adapter.removeOne(action.payload.ficha.id, {
        ...state,
        loading: false
      });
    }

    case FichaActionTypes.RegistrarIngresoSuccess: {
      return adapter.upsertOne(action.payload.ficha, {
        ...state,
        loading: false
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

export const getFichasLoading = (state: State) => state.loading;
export const getFichasLoaded = (state: State) => state.loaded;
export const getFichasFilter = (state: State) => state.filter;
