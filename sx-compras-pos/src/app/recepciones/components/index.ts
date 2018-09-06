import { ComsTableComponent } from './coms-table/coms-table.component';
import { ComFormComponent } from './com-form/com-form.component';
import { ComPartidasComponent } from './com-partidas/com-partidas.component';
import { ComprasSelectorComponent } from './compras-selector/compras-selector.component';
import { ComprasSelectorTableComponent } from './compras-selector/compras-selector-table.component';
import { ComCreateFormComponent } from './com-create-form/com-create-form.component';
import { ComsFilterDialogComponent } from './coms-filter/coms-filter-dialog.component';
import { ComsFilterComponent } from './coms-filter/coms-filter.component';
import { ComsFilterLabelComponent } from './coms-filter/coms-filter-label.component';

export const components: any[] = [
  ComsTableComponent,
  ComFormComponent,
  ComCreateFormComponent,
  ComPartidasComponent,
  ComprasSelectorComponent,
  ComprasSelectorTableComponent,
  ComsFilterComponent,
  ComsFilterDialogComponent,
  ComsFilterLabelComponent
];

export const entryComponents: any[] = [
  ComprasSelectorComponent,
  ComsFilterDialogComponent
];

export * from './coms-table/coms-table.component';
export * from './com-form/com-form.component';
export * from './com-create-form/com-create-form.component';
export * from './com-partidas/com-partidas.component';
export * from './compras-selector/compras-selector.component';
export * from './compras-selector/compras-selector-table.component';

export * from './coms-filter/coms-filter.component';
export * from './coms-filter/coms-filter-dialog.component';
export * from './coms-filter/coms-filter-label.component';
