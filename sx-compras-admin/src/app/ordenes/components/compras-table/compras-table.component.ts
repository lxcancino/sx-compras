import {
  Component,
  OnInit,
  ChangeDetectionStrategy,
  Input,
  OnChanges,
  SimpleChanges,
  ViewChild,
  Output,
  EventEmitter,
  OnDestroy
} from '@angular/core';
import { MatTableDataSource, MatPaginator, MatSort } from '@angular/material';

import { Subscription } from 'rxjs';

import { Compra } from '../../models/compra';

@Component({
  selector: 'sx-compras-table',
  changeDetection: ChangeDetectionStrategy.OnPush,
  templateUrl: './compras-table.component.html',
  styleUrls: ['./compras-table.component.scss']
})
export class ComprasTableComponent implements OnInit, OnChanges {
  @Input()
  compras: Compra[] = [];

  @Input()
  filter;
  dataSource = new MatTableDataSource<Compra>([]);

  displayColumns = [
    'sucursalNombre',
    'folio',
    'fecha',
    'proveedor',
    'comentario',
    'moneda',
    'tipoDeCambio',
    'total',
    'modificada',
    'lastUpdatedBy',
    'pendiente',
    'cerrada',
    'ultimaDepuracion'
    // 'operaciones'
  ];

  @ViewChild(MatSort)
  sort: MatSort;
  @ViewChild(MatPaginator)
  paginator: MatPaginator;
  @Output()
  select = new EventEmitter();
  @Output()
  edit = new EventEmitter();

  constructor() {}

  ngOnInit() {
    this.dataSource.paginator = this.paginator;
    this.dataSource.sort = this.sort;
  }

  ngOnChanges(changes: SimpleChanges) {
    if (changes.compras && changes.compras.currentValue) {
      this.dataSource.data = changes.compras.currentValue;
    }
    if (changes.filter) {
      this.dataSource.filter = changes.filter.currentValue;
    }
  }

  onEdit($event: Event, row) {
    $event.preventDefault();
    this.edit.emit(row);
  }

  getPrintUrl(event: Compra) {
    return `compras/print/${event.id}`;
  }
}
