import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule, ReactiveFormsModule } from '@angular/forms';

import { MaterialModule } from './_material/material.module';
import { CovalentModule } from './_covalent/covalent.module';
import { AgGridModule } from 'ag-grid-angular';
// components
import {
  components,
  entyComponents,
  NumericEditorComponent
} from './components';
// Directives
import { directives } from './directives';
import { pipes } from './pipes';
import { MAT_DATE_LOCALE } from '@angular/material';

@NgModule({
  imports: [
    CommonModule,
    FormsModule,
    ReactiveFormsModule,
    MaterialModule,
    CovalentModule,
    // AgGridModule.withComponents([])
    AgGridModule.withComponents([NumericEditorComponent])
  ],
  exports: [
    CommonModule,
    FormsModule,
    ReactiveFormsModule,
    MaterialModule,
    CovalentModule,
    AgGridModule,
    // AgGridModule.withComponents([NumericEditorComponent]),
    ...components,
    ...entyComponents,
    ...directives,
    ...pipes
  ],
  declarations: [...components, ...directives, ...pipes],
  entryComponents: [...entyComponents],
  providers: [{ provide: MAT_DATE_LOCALE, useValue: 'es-MX' }]
})
export class SharedModule {}
