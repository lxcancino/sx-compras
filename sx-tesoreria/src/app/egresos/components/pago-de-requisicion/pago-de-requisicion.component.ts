import { Component, OnInit, Inject, OnDestroy } from '@angular/core';
import { MAT_DIALOG_DATA } from '@angular/material';
import { FormGroup, FormBuilder, Validators } from '@angular/forms';

import { Subscription } from 'rxjs';

import { Requisicion } from '../../models';

import * as moment from 'moment';

@Component({
  selector: 'sx-pago-requisicion',
  template: `
  <form [formGroup]="form">
    <h2 mat-dialog-title>Pago de la requisicion {{requisicion.folio}}</h2>
    <mat-dialog-content>
      <div layout="column">

        <sx-cuenta-banco-field formControlName="cuenta" disponibleEnPagos="true"></sx-cuenta-banco-field>
        <div layout>
          <mat-form-field flex>
            <input matInput [matDatepicker]="myDatepicker" placeholder="Fecha"
              formControlName="fecha" autocomplete="off">
            <mat-datepicker-toggle matSuffix [for]="myDatepicker"></mat-datepicker-toggle>
            <mat-datepicker #myDatepicker></mat-datepicker>
          </mat-form-field>
          <div layout>
            <mat-form-field class="pad-left" flex>
              <input matInput formControlName="referencia" placeholder="Referencia" autocomplete="off">
            </mat-form-field>
            <mat-form-field class="pad-left" flex>
              <input matInput formControlName="cheque" placeholder="Próximo cheque" autocomplete="off">
            </mat-form-field>
          </div>
        </div>

      </div>

    </mat-dialog-content>

    <mat-dialog-actions>
      <button mat-button [mat-dialog-close]="form.value" [disabled]="form.invalid">Aceptar</button>
      <button mat-button mat-dialog-close>Canelar</button>
    </mat-dialog-actions>
  </form>
  `
})
export class PagoDeRequisicionComponent implements OnInit, OnDestroy {
  form: FormGroup;
  requisicion: Requisicion;
  subscription: Subscription;

  constructor(
    @Inject(MAT_DIALOG_DATA) public data: any,
    private fb: FormBuilder
  ) {
    this.requisicion = data.requisicion;
  }

  ngOnInit() {
    this.buildForm();
    this.subscription = this.form.get('cuenta').valueChanges.subscribe(cta => {
      if (this.requisicion.formaDePago === 'CHEQUE') {
        this.form.get('cheque').setValue(cta.proximoCheque);
        this.form.get('referencia').setValue(cta.proximoCheque);
        this.form.get('referencia').disable();
      } else {
        this.form.get('referencia').setValue('');
        this.form.get('referencia').enable();
      }
    });
  }

  ngOnDestroy() {
    if (this.subscription) {
      this.subscription.unsubscribe();
    }
  }

  private buildForm() {
    this.form = this.fb.group({
      fecha: [{ value: this.pago, disabled: true }, [Validators.required]],
      cuenta: [null, Validators.required],
      referencia: [null, Validators.required],
      cheque: [{ value: null, disabled: true }]
    });
  }

  get pago() {
    return moment(this.requisicion.fechaDePago).toDate();
  }
}
