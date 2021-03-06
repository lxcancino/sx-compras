import {
  Component,
  Input,
  Output,
  EventEmitter,
  OnInit,
  ChangeDetectionStrategy,
  OnChanges,
  SimpleChanges
} from '@angular/core';
import {
  FormGroup,
  FormBuilder,
  Validators,
  FormArray,
  FormControl
} from '@angular/forms';

import { Compra, CompraDet, actualizarPartida } from '../../models';
import { ProveedorProducto } from 'app/proveedores/models/proveedorProducto';

@Component({
  selector: 'sx-compra-form2',
  changeDetection: ChangeDetectionStrategy.OnPush,
  templateUrl: './compra-form2.component.html',
  styleUrls: ['./compra-form2.component.scss']
})
export class CompraForm2Component implements OnInit, OnChanges {
  @Input() compra: Partial<Compra>;
  @Input() productos: ProveedorProducto[];
  @Output() save = new EventEmitter<Partial<Compra>>();
  @Output() delete = new EventEmitter<Compra>();

  form: FormGroup;
  constructor(private fb: FormBuilder) {}

  ngOnInit() {}

  ngOnChanges(changes: SimpleChanges) {
    if (changes.compra.isFirstChange) {
    }
    if (!this.form) {
      this.buildForm();
    }
    if (changes.compra && changes.compra.currentValue) {
      // console.log('Editando compra:', changes.compra.currentValue);
      const comp = changes.compra.currentValue;
      this.cleanPartidas();
      this.form.patchValue(comp);

      comp.partidas.forEach(item => this.partidas.push(new FormControl(item)));
      if (comp.id) {
        this.form.get('proveedor').disable();
        this.form.get('sucursal').disable();
        this.form.get('especial').disable();
        this.form.get('moneda').disable();
        if (this.compra.status === 'A') {
          this.form.disable();
        }
      }
    }
  }

  private cleanPartidas() {
    while (this.partidas.length !== 0) {
      this.partidas.removeAt(0);
    }
  }

  buildForm() {
    this.form = this.fb.group({
      fecha: [new Date(), [Validators.required]],
      entrega: [null],
      proveedor: [null, [Validators.required]],
      sucursal: [null],
      moneda: ['MXN', [Validators.required]],
      tipoDeCambio: [1.0, [Validators.required, Validators.min(1)]],
      comentario: [],
      partidas: this.fb.array([])
    });
  }

  onSave() {
    if (this.form.valid) {
      let fecha = this.form.value.fecha;
      if (fecha instanceof Date) {
        fecha = fecha.toISOString();
      }
      let entrega = this.form.get('entrega').value;
      if (entrega instanceof Date) {
        entrega = fecha.toISOString();
      }
      const proveedor = { id: this.proveedor.id };
      const sucursal = { id: this.form.get('sucursal').value.id };
      const res = {
        ...this.compra,
        ...this.form.value,
        proveedor,
        sucursal,
        fecha,
        entrega,
        ...this.prepararPartidas()
      };
      this.save.emit(res);
      this.form.markAsPristine();
    }
  }

  prepararPartidas(): CompraDet[] {
    const partidas = [...this.partidas.value];
    partidas.forEach(item => {
      if (!item.sucursal) {
        item.sucursal = this.sucursal.id;
      }
    });
    return partidas;
  }

  onEditPartida(index: number) {
    const control = this.partidas.at(index);
    const det: CompraDet = control.value;
    actualizarPartida(det);
    this.partidas.setControl(index, new FormControl(det));
    this.form.markAsDirty();
  }

  onInsertPartida(event: CompraDet) {
    actualizarPartida(event);
    this.partidas.push(new FormControl(event));
    this.form.markAsDirty();
  }

  onDeletePartida(index: number) {
    this.partidas.removeAt(index);
    this.form.markAsDirty();
  }

  onDepurar(index: number) {
    if (this.compra && this.compra.status !== 'A') {
      const control = this.partidas.at(index);
      const partida: CompraDet = { ...control.value };

      partida.depurado = partida.porRecibir;
      partida.depuracion = new Date().toISOString();

      this.partidas.setControl(index, new FormControl(partida));
      this.form.markAsDirty();
    }
  }

  get partidas() {
    return this.form.get('partidas') as FormArray;
  }

  get proveedor() {
    return this.form.get('proveedor').value;
  }
  get moneda() {
    return this.form.get('moneda').value;
  }
  get sucursal() {
    return this.compra ? this.compra.sucursal : null;
  }
}
