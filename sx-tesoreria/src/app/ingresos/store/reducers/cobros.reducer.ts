import { EntityState, EntityAdapter, createEntityAdapter } from '@ngrx/entity';

import { Cobro, CobrosFilter, createCobrosFilter } from '../../models/cobro';
import { CobroActions, CobroActionTypes } from '../actions/cobros.actions';

import * as moment from 'moment';

export interface State extends EntityState<Cobro> {
  loading: boolean;
  loaded: boolean;
  filter: CobrosFilter;
  term: string;
}

export function comparLastUpdate(row1: Cobro, row2: Cobro): number {
  const d1 = moment(row1.lastUpdated);
  const d2 = moment(row2.lastUpdated);
  return d1.isSameOrBefore(d2) ? 1 : -1;
}

export const adapter: EntityAdapter<Cobro> = createEntityAdapter<Cobro>({
  sortComparer: comparLastUpdate
});

export const initialState: State = adapter.getInitialState({
  loading: false,
  loaded: false,
  filter: createCobrosFilter(),
  term: ''
});

export function reducer(state = initialState, action: CobroActions): State {
  switch (action.type) {
    case CobroActionTypes.SetCobrosFilter: {
      const filter = action.payload.filter;
      return {
        ...state,
        filter
      };
    }
    case CobroActionTypes.SetCobrosSearchTerm: {
      const term = action.payload.term;
      return {
        ...state,
        term
      };
    }

    case CobroActionTypes.DevolverCheque:
    case CobroActionTypes.DeleteCobro:
    case CobroActionTypes.CreateCobro:
    case CobroActionTypes.LoadCobros: {
      return {
        ...state,
        loading: true
      };
    }

    case CobroActionTypes.DevolverChequeFail:
    case CobroActionTypes.DeleteCobroFail:
    case CobroActionTypes.CreateCobroFail:
    case CobroActionTypes.UpdateCobroFail:
    case CobroActionTypes.LoadCobrosFail: {
      return {
        ...state,
        loading: false
      };
    }
    case CobroActionTypes.LoadCobrosSuccess: {
      return adapter.addAll(action.payload.cobros, {
        ...state,
        loading: false,
        loaded: true
      });
    }

    case CobroActionTypes.DevolverChequeSuccess:
    case CobroActionTypes.UpdateCobroSuccess: {
      const cobro = action.payload.cobro;
      return adapter.updateOne(
        {
          id: cobro.id,
          changes: cobro
        },
        {
          ...state,
          loading: false
        }
      );
    }

    case CobroActionTypes.UpsertCobro: {
      return adapter.upsertOne(action.payload.cobro, {
        ...state,
        loading: false
      });
    }

    case CobroActionTypes.CreateCobroSuccess: {
      return adapter.addOne(action.payload.cobro, {
        ...state,
        loading: false
      });
    }

    case CobroActionTypes.DeleteCobroSuccess: {
      return adapter.removeOne(action.payload.cobro.id, {
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

export const getCobrosLoading = (state: State) => state.loading;
export const getCobrosLoaded = (state: State) => state.loaded;
export const getCobrosFilter = (state: State) => state.filter;
export const getCobrosSearchTerm = (state: State) => state.term;
