import {
  Component,
  OnInit,
  ChangeDetectionStrategy,
  Input,
  OnChanges,
  SimpleChanges,
  ViewChild,
  Output,
  EventEmitter
} from '@angular/core';
import { MatTableDataSource, MatSort } from '@angular/material';
import { NotaDeCreditoDet } from 'app/cobranza/models';

import * as _ from 'lodash';
import { FormGroup } from '@angular/forms';

@Component({
  selector: 'sx-bonificacion-form-partidas',
  templateUrl: './bonificacion-form-partidas.component.html',
  styleUrls: ['./bonificacion-form-partidas.component.scss'],
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class BonificacionFormPartidasComponent implements OnInit, OnChanges {
  @Input()
  partidas: NotaDeCreditoDet[] = [];

  @Input() parent: FormGroup;

  @Input()
  filter: string;

  @Input() disabled = false;

  dataSource = new MatTableDataSource<NotaDeCreditoDet>([]);

  @Input() displayColumns = [
    'renglon',
    'sucursal',
    'factura',
    'uuid',
    'facturaFecha',
    'facturaTotal',
    'facturaPagos',
    'facturaSaldo',
    'base',
    'impuesto',
    'importe',
    'operaciones'
  ];

  @ViewChild(MatSort)
  sort: MatSort;

  @Output()
  edit = new EventEmitter();

  @Output()
  select = new EventEmitter();

  @Output()
  delete = new EventEmitter();

  constructor() {}

  ngOnInit() {
    this.dataSource.sort = this.sort;
  }

  ngOnChanges(changes: SimpleChanges) {
    if (changes.partidas && changes.partidas.currentValue) {
      this.dataSource.data = changes.partidas.currentValue;
    }
    if (changes.filter) {
      const s = changes.filter.currentValue || '';
      this.dataSource.filter = s.toLowerCase();
    }
  }

  doDelete(event: Event, index: number, row: Partial<NotaDeCreditoDet>) {
    event.stopPropagation();
    this.delete.emit({ index, row });
  }

  doSelect(event: Event, row: NotaDeCreditoDet) {
    event.stopPropagation();
    this.select.emit(row);
  }

  sumBy(property: string) {
    return _.sumBy(this.partidas, property);
  }
}
