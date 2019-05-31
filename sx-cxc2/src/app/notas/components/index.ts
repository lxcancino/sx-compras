import { NotasTableComponent } from './notas-table/notas-table.component';
import { BonificacionesTableComponent } from './bonificaciones-table/bonificaciones-table.component';
import { NotaFormComponent } from './nota-form/nota-form.component';
import { NotaHeaderComponent } from './nota-header/nota-header.component';
import { NotaCreateModalComponent } from './nota-create-modal/nota-create-modal.component';
import { NotaCreateBtnComponent } from './nota-create-modal/nota-create-btn.component';

export const components: any[] = [
  NotasTableComponent,
  BonificacionesTableComponent,
  NotaFormComponent,
  NotaHeaderComponent,
  NotaCreateBtnComponent
];
export const entryComponents: any[] = [NotaCreateModalComponent];

export * from './notas-table/notas-table.component';
export * from './bonificaciones-table/bonificaciones-table.component';
export * from './nota-form/nota-form.component';
export * from './nota-header/nota-header.component';
export * from './nota-create-modal/nota-create-btn.component';
export * from './nota-create-modal/nota-create-modal.component';
