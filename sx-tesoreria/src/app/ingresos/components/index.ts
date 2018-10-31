import { CobrosTableComponent } from './cobros-table/cobros-table.component';
import { CobrosFilterComponent } from './cobros-filter/cobros-filter.component';
import { CobrosFilterBtnComponent } from './cobros-filter/cobros-filter-btn.component';
import { CobrosFilterLabelComponent } from './cobros-filter/cobros-filter-label.component';
import { CobroFormComponent } from './cobro-form/cobro-form-component';
import { CobrosChequeTableComponent } from './selecor-de-cheques/cobros-cheque-table.component';
// Cheques devueltos
import { ChequesDevueltosTableComponent } from './cheques-devueltos/cheques-devueltos-table.component';
import { SelectorDeCobrosChequeComponent } from './selecor-de-cheques/selector-de-cobros-cheques.component';
import { ChequeDevueltoFormComponent } from './cheque-devuelto-form/cheque-devuelto-form.component';
import { FichasFilterComponent } from './fichas-filter/fichas-filter.component';

export const components: any[] = [
  // Cobros
  CobrosTableComponent,
  CobrosFilterComponent,
  CobrosFilterBtnComponent,
  CobrosFilterLabelComponent,
  CobroFormComponent,
  // Cheques devueltos
  ChequesDevueltosTableComponent,
  SelectorDeCobrosChequeComponent,
  CobrosChequeTableComponent,
  ChequeDevueltoFormComponent,
  // Fichas
  FichasFilterComponent
];
export const entryComponents: any[] = [
  CobrosFilterComponent,
  CobroFormComponent,
  SelectorDeCobrosChequeComponent,
  ChequeDevueltoFormComponent
];

// Cobros
export * from './cobros-table/cobros-table.component';
export * from './cobros-filter/cobros-filter.component';
export * from './cobros-filter/cobros-filter-btn.component';
export * from './cobros-filter/cobros-filter-label.component';
export * from './cobro-form/cobro-form-component';

// Cheques devueltos
export * from './cheques-devueltos/cheques-devueltos-table.component';
export * from './selecor-de-cheques/selector-de-cobros-cheques.component';
export * from './selecor-de-cheques/cobros-cheque-table.component';
export * from './cheque-devuelto-form/cheque-devuelto-form.component';
// Fichas
export * from './fichas-filter/fichas-filter.component';
