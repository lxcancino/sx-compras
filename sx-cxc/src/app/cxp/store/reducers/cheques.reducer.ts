import { EntityState, EntityAdapter, createEntityAdapter } from '@ngrx/entity';

import { Cheque, ChequesFilter, createChequesFilter } from '../../model';
import { ChequeActions, ChequeActionTypes } from '../actions/cheque.actions';

export interface State extends EntityState<Cheque> {
  loading: boolean;
  loaded: boolean;
  filter: ChequesFilter;
  term: string;
}

export function sortByFolio(a: Cheque, b: Cheque): number {
  return b.folio > a.folio ? 1 : b.folio <= a.folio ? -1 : 0;
}

export const adapter: EntityAdapter<Cheque> = createEntityAdapter<Cheque>({
  sortComparer: sortByFolio
});

export const initialState: State = adapter.getInitialState({
  loading: false,
  loaded: false,
  filter: createChequesFilter(),
  term: ''
});

export function reducer(state = initialState, action: ChequeActions): State {
  switch (action.type) {
    case ChequeActionTypes.SetChequesFilter: {
      const filter = action.payload.filter;
      return {
        ...state,
        filter
      };
    }
    case ChequeActionTypes.SetChequesSearchTerm: {
      const term = action.payload.term;
      return {
        ...state,
        term
      };
    }

    case ChequeActionTypes.LoadCheques: {
      return {
        ...state,
        loading: true
      };
    }

    case ChequeActionTypes.UpdateChequeFail:
    case ChequeActionTypes.LoadChequesFail: {
      return {
        ...state,
        loading: false
      };
    }
    case ChequeActionTypes.LoadChequesSuccess: {
      return adapter.addAll(action.payload.cheques, {
        ...state,
        loading: false,
        loaded: true
      });
    }

    case ChequeActionTypes.UpdateChequeSuccess: {
      const cheque = action.payload.cheque;
      return adapter.updateOne(
        {
          id: cheque.id,
          changes: cheque
        },
        {
          ...state,
          loading: false
        }
      );
    }

    case ChequeActionTypes.UpsertCheque: {
      return adapter.upsertOne(action.payload.cheque, {
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

export const getChequesLoading = (state: State) => state.loading;
export const getChequesLoaded = (state: State) => state.loaded;
export const getChequesFilter = (state: State) => state.filter;
export const getChequesSearchTerm = (state: State) => state.term;
