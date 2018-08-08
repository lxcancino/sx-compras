import {
  Component,
  OnInit,
  Input,
  ChangeDetectionStrategy,
  Output,
  EventEmitter,
  OnDestroy,
  OnChanges,
  SimpleChanges
} from '@angular/core';
import {
  FormBuilder,
  FormGroup,
  FormArray,
  Validators,
  FormControl
} from '@angular/forms';

import { Contrarecibo, CuentaPorPagar } from '../../model';

import * as _ from 'lodash';
import * as moment from 'moment';

@Component({
  selector: 'sx-recibo-form',
  changeDetection: ChangeDetectionStrategy.OnPush,
  templateUrl: './recibo-form.component.html',
  styleUrls: ['./recibo-form.component.scss']
})
export class ReciboFormComponent implements OnInit, OnDestroy, OnChanges {
  @Input() recibo: Partial<Contrarecibo>;
  @Input() facturas: CuentaPorPagar[];
  @Output() save = new EventEmitter();
  @Output() cancel = new EventEmitter();
  @Output() delete = new EventEmitter();
  filtro: string;

  form: FormGroup;
  constructor(private fb: FormBuilder) {}

  ngOnInit() {}

  ngOnDestroy() {}

  ngOnChanges(changes: SimpleChanges) {
    this.buildForm();
    if (changes.recibo && changes.recibo.currentValue) {
      console.log('Editando recibo: ', this.recibo);
      this.setRecibo();
    }
    if (changes.facturas && changes.facturas.currentValue) {
    }
  }

  setRecibo() {
    this.form.patchValue(this.recibo);
    if (this.recibo) {
      this.form.get('fecha').disable();
      this.form.get('moneda').disable();
    }
    // this.form.get('fecha').setValue(moment(this.recibo.fecha));
    this.cleanPartidas();
    this.recibo.partidas.forEach(det => {
      this.partidas.push(new FormControl(det));
    });
  }

  private buildForm() {
    if (!this.form) {
      this.form = this.fb.group({
        proveedor: [null, [Validators.required]],
        fecha: [new Date(), [Validators.required]],
        moneda: ['MXN', [Validators.required]],
        comentario: [],
        partidas: this.fb.array([])
      });
    }
  }

  private cleanPartidas() {
    while (this.partidas.length !== 0) {
      this.partidas.removeAt(0);
    }
  }

  onSubmit() {
    if (this.form.valid && !this.form.disabled) {
      let entity = {};
      if (this.recibo) {
        entity = {
          id: this.recibo.id,
          partidas: this.prepararPartidas()
        };
      } else {
        const proveedor: any = this.form.value.proveedor;
        let fecha = this.form.value.fecha;
        if (fecha instanceof Date) {
          fecha = fecha.toISOString();
        }
        entity = {
          ...this.form.value,
          proveedor: { id: proveedor.id },
          fecha,
          partidas: this.prepararPartidas()
        };
      }
      this.save.emit(entity);
      this.form.markAsPristine();
    }
  }

  prepararPartidas(): Partial<CuentaPorPagar>[] {
    const data: CuentaPorPagar[] = this.partidas.value;
    return data.map(item => {
      return { id: item.id };
    });
    /*
    const partidas = [...this.partidas.value];
    partidas.forEach(item => {});
    return partidas;
    */
  }

  get partidas() {
    return this.form.get('partidas') as FormArray;
  }

  agregarFacturas(selected: CuentaPorPagar[]) {
    selected.forEach(cxp => {
      const found = _.find(this.partidas.value, ['uuid', cxp.uuid]);
      if (!found) {
        this.partidas.push(new FormControl(cxp));
      }
    });
    this.form.markAsDirty();
  }

  onDeleteRow(index: number) {
    this.partidas.removeAt(index);
    this.form.markAsDirty();
  }

  get total() {
    return _.sumBy(this.partidas.value, 'total');
  }

  get proveedor() {
    return this.form.get('proveedor').value;
  }
}
