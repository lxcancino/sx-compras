import { Component, OnInit, Inject } from '@angular/core';
import { MAT_DIALOG_DATA } from '@angular/material';
import { FormGroup, FormBuilder, Validators } from '@angular/forms';

import { EnviosFilter } from '../../model';

@Component({
  selector: 'sx-envios-filter',
  template: `
  <form [formGroup]="form">
    <h2 mat-dialog-title>{{title}}</h2>
    <mat-dialog-content>
      <div layout>
        <mat-form-field flex>
          <input matInput [matDatepicker]="myDatepicker" placeholder="Fecha inicial" formControlName="fechaInicial" autocomplete="off">
          <mat-datepicker-toggle matSuffix [for]="myDatepicker"></mat-datepicker-toggle>
          <mat-datepicker #myDatepicker></mat-datepicker>
        </mat-form-field>
        <mat-form-field flex class="pad-left pad-right">
          <input matInput [matDatepicker]="myDatepicker2" placeholder="Fecha final" formControlName="fechaFinal" autocomplete="off">
          <mat-datepicker-toggle matSuffix [for]="myDatepicker2"></mat-datepicker-toggle>
          <mat-datepicker #myDatepicker2></mat-datepicker>
        </mat-form-field>
      </div>
      <sx-sucursal-field [parent]="form"></sx-sucursal-field>
      <div layout="column">
        <h3>Registros {{form.value.registros}}</h3>
        <mat-slider thumbLabel step="10" tickInterval="100" min="50" max="5000" formControlName="registros" flex></mat-slider>
      </div>
    </mat-dialog-content>

    <mat-dialog-actions>
      <button mat-button [mat-dialog-close]="form.value" [disabled]="form.invalid">Aceptar</button>
      <button mat-button mat-dialog-close>Canelar</button>
    </mat-dialog-actions>
  </form>
  `
})
export class EnviosFilterComponent implements OnInit {
  title;
  form: FormGroup;
  filter: EnviosFilter;

  constructor(
    @Inject(MAT_DIALOG_DATA) public data: any,
    private fb: FormBuilder
  ) {
    this.title = data.title || 'Filtrar envios';
    this.filter = data.filter;
  }

  ngOnInit() {
    this.buildForm();
    if (this.filter) {
      this.form.patchValue(this.filter);
    }
  }

  private buildForm() {
    this.form = this.fb.group({
      fechaInicial: [null, [Validators.required]],
      fechaFinal: [null, [Validators.required]],
      sucursal: [null],
      registros: [
        10,
        [Validators.required, Validators.min(10), Validators.max(3000)]
      ]
    });
  }
}