import { Component, OnInit, Inject, OnDestroy } from '@angular/core';
import { MAT_DIALOG_DATA } from '@angular/material';
import { FormGroup, FormBuilder, Validators } from '@angular/forms';

import { Subscription } from 'rxjs';

import { Rembolso } from '../../models';

import * as moment from 'moment';
import { CuentaDeBanco } from 'app/models';

@Component({
  selector: 'sx-rembolso-pago-dialog',
  template: `
  <form [formGroup]="form">
    <span mat-dialog-title >
      <div layout>
        <span >Pago rembolso: {{rembolso.id}}</span>
        <span flex></span>
        <span> Importe: </span>
        <span>{{rembolso.apagar | currency}}</span>
      </div>
    </span>
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
            <mat-form-field class="pad-left" flex *ngIf="rembolso.formaDePago === 'CHEQUE'">
              <input matInput formControlName="cheque" placeholder="Próximo cheque" autocomplete="off">
            </mat-form-field>
            <mat-form-field class="pad-left" flex *ngIf="rembolso.formaDePago === 'TRANSFERENCIA'">
              <input matInput formControlName="comision" placeholder="Comisión" autocomplete="off">
              <span matPrefix>$&nbsp;</span>
              <mat-hint align="end">Pesos + IVA</mat-hint>
            </mat-form-field>
          </div>
        </div>
        <div layout>
          <mat-form-field>
            <input matInput placeholder="Importe a pagar" formControlName="importe" autocomplete="off" type="number">
          </mat-form-field>
          <mat-slide-toggle [checked]="false" (change)="manual($event)" >
            Asignación manual de cheque
          </mat-slide-toggle>
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
export class RembolsoPagoDialogComponent implements OnInit, OnDestroy {
  form: FormGroup;
  rembolso: Rembolso;
  subscription: Subscription;

  constructor(
    @Inject(MAT_DIALOG_DATA) public data: any,
    private fb: FormBuilder
  ) {
    this.rembolso = data.rembolso;
  }

  ngOnInit() {
    this.buildForm();
    this.subscription = this.form.get('cuenta').valueChanges.subscribe(cta => {
      if (this.rembolso.formaDePago === 'CHEQUE') {
        this.form.get('cheque').setValue(cta.proximoCheque);
        this.form.get('referencia').setValue(cta.proximoCheque);
        this.form.get('referencia').disable();
      } else {
        this.form.get('referencia').setValue('');
        this.form.get('referencia').enable();
      }
      if (this.rembolso.formaDePago === 'TRANSFERENCIA') {
        if (cta.comisionPorTransferencia) {
          this.form.get('comision').setValue(cta.comisionPorTransferencia);
        }
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
      comision: [{ value: 0.0, disabled: true }],
      cheque: [{ value: null, disabled: true }],
      importe: [null]
    });
  }

  get pago() {
    return moment(this.rembolso.fechaDePago).toDate();
  }

  manual(event) {
    if (event.checked) {
      this.form.get('referencia').enable();
    } else {
      this.form.get('referencia').disable();
    }
  }
}
