import { Component, OnInit, Inject } from '@angular/core';
import { MAT_DIALOG_DATA } from '@angular/material';
import { FormGroup, FormBuilder, Validators } from '@angular/forms';

import { CarteraFilter } from '../../models';

@Component({
  selector: 'sx-cartera-filter',
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

      <div layout>
        <mat-form-field flex>
          <input matInput formControlName="nombre" placeholder="Cliente">
        </mat-form-field>
      </div>
      <div layout>
        <mat-form-field flex>
          <mat-select placeholder="Cartera" formControlName="tipo">
            <mat-option *ngFor="let tipo of tipos" [value]="tipo.tipo">{{ tipo.descripcion }}
            </mat-option>
          </mat-select>
        </mat-form-field>
      </div>

      <div layout="column">
        <h3>Registros {{form.value.registros}}</h3>
        <mat-slider thumbLabel step="10" tickInterval="10" min="10" max="200" formControlName="registros" flex></mat-slider>
      </div>
    </mat-dialog-content>

    <mat-dialog-actions>
      <button mat-button [mat-dialog-close]="form.value" [disabled]="form.invalid">Aceptar</button>
      <button mat-button mat-dialog-close>Canelar</button>
    </mat-dialog-actions>
  </form>
  `
})
export class CarteraFilterComponent implements OnInit {
  title;
  form: FormGroup;
  filter: CarteraFilter;
  tipos: Array<{ tipo: string; descripcion: string }> = [
    { tipo: 'CRE', descripcion: 'CREDITO' },
    { tipo: 'JUR', descripcion: 'JURIDICO' },
    { tipo: 'CHE', descripcion: 'CHEQUE_DEVUELTO' },
    { tipo: 'CHO', descripcion: 'CHOFERES' },
    { tipo: 'TODOS', descripcion: 'TODAS' }
  ];

  constructor(
    @Inject(MAT_DIALOG_DATA) public data: any,
    private fb: FormBuilder
  ) {
    this.title = data.title || 'FILTRO DE CARTERA';
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
      tipo: [{ value: 'CRE', disabled: true }, [Validators.required]],
      nombre: [null],
      registros: [
        50,
        [Validators.required, Validators.min(10), Validators.max(300)]
      ]
    });
  }
}
