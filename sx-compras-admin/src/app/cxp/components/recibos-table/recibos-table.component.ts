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
import { MatTableDataSource, MatPaginator, MatSort } from '@angular/material';

import { Contrarecibo } from '../../model';

@Component({
  selector: 'sx-recibos-table',
  changeDetection: ChangeDetectionStrategy.OnPush,
  templateUrl: './recibos-table.component.html',
  styleUrls: ['./recibos-table.component.scss']
})
export class RecibosTableComponent implements OnInit, OnChanges {
  @Input() recibos: Contrarecibo[] = [];
  @Input() filter: string;
  dataSource = new MatTableDataSource<Contrarecibo>([]);

  @Input()
  displayColumns = [
    'folio',
    'nombre',
    'fecha',
    'moneda',
    'total',
    'comentario',
    'modificado',
    'operaciones'
  ];
  @ViewChild(MatSort) sort: MatSort;
  @ViewChild(MatPaginator) paginator: MatPaginator;

  @Output() select = new EventEmitter();
  // @Output() edit = new EventEmitter();

  constructor() {}

  ngOnInit() {
    this.dataSource.paginator = this.paginator;
    this.dataSource.sort = this.sort;
  }

  ngOnChanges(changes: SimpleChanges) {
    if (changes.recibos && changes.recibos.currentValue) {
      this.dataSource.data = changes.recibos.currentValue;
    }
    if (changes.filter) {
      this.dataSource.filter = changes.filter.currentValue.toLowerCase();
    }
  }

  toogleSelect(event: Contrarecibo) {
    event.selected = !event.selected;
    const data = this.recibos.filter(item => item.selected);
    this.select.emit([...data]);
  }
  /*
  onEdit($event: Event, row) {
    $event.preventDefault();
    this.edit.emit(row);
  }
  */
}
