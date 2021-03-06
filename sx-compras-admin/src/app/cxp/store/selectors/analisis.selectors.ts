import { createSelector } from '@ngrx/store';

import * as fromRoot from 'app/store';
import * as fromFeature from '../reducers';
import * as fromAnalisis from '../reducers/analisis.reducer';
import { Analisis } from '../../model/analisis';

import * as _ from 'lodash';

export const getAnalisisState = createSelector(
  fromFeature.getCxpState,
  (state: fromFeature.CxpState) => state.analisis
);

export const getAnalisisEntities = createSelector(
  getAnalisisState,
  fromAnalisis.getAnalisisEntities
);

export const getAllAnalisis = createSelector(
  getAnalisisEntities,
  entities => Object.keys(entities).map(id => entities[id])
);

export const getAnalisisLoading = createSelector(
  getAnalisisState,
  fromAnalisis.getAnalisisLoading
);

export const getAnalisisLoaded = createSelector(
  getAnalisisState,
  fromAnalisis.getAnalisisLoaded
);

// Analisis form selectors
export const getFacturasPendientesEntities = createSelector(
  getAnalisisState,
  fromAnalisis.getFacturasPendientes
);

export const getAllFacturasPendientes = createSelector(
  getFacturasPendientesEntities,
  entities => _.sortBy(Object.keys(entities).map(id => entities[id]), 'folio')
);

export const getSelectedAnalisis = createSelector(
  getAnalisisEntities,
  fromRoot.getRouterState,
  (entities, router): Analisis => {
    return router.state && entities[router.state.params.analisisId];
  }
);

export const getComsPendientesEntities = createSelector(
  getAnalisisState,
  fromAnalisis.getComsPendientes
);

export const getAllComsPendientes = createSelector(
  getComsPendientesEntities,
  entities => Object.keys(entities).map(id => entities[id])
);

export const getAnalisisPeriodo = createSelector(
  getAnalisisState,
  fromAnalisis.getAnalisisPeriodo
);

export const getAnalisisFilter = createSelector(
  getAnalisisState,
  fromAnalisis.getAnalisisFilter
);

export const getFilteredAnalisis = createSelector(
  getAllAnalisis,
  getAnalisisFilter,
  (analisis, filter: any) => {
    let filtered = [...analisis];
    if (filter.tipo) {
      if (filter.tipo === 'Pendientes') {
        filtered = _.filter(filtered, item => !item.cerrado);
      } else if (filter.tipo === 'Cerradas') {
        // filtered = _.filter(filtered, item => item.cerrado);
      }
    }
    return filtered;
  }
);
