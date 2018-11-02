import { Component, OnInit } from '@angular/core';

import { Store, select } from '@ngrx/store';
import * as fromStore from '../../store';

import { Observable } from 'rxjs';

import { Ficha, FichaFilter } from '../../models';
import { ReportService } from 'app/reportes/services/report.service';

import { MatDialog } from '@angular/material';
import { TdDialogService } from '@covalent/core';

import * as _ from 'lodash';

@Component({
  selector: 'sx-fichas',
  template: `
    <mat-card>
    <div layout layout-align="start center"  class="pad-left-sm pad-right-sm">
      <span class="push-left-sm">
        <span class="mat-title">Fichas registrados</span>
        </span>
        <span layout *ngIf="fichas$ | async as fichas" class="tc-indigo-600 pad">

        <span layout>
          <span class="pad-left">Efectivo: </span>
          <span class="pad-left">{{getTotal(fichas, 'EFECTIVO') | currency}}</span>
        </span>
        <span layout>
          <span class="pad-left">Otros: </span>
          <span class="pad-left">{{getTotal(fichas, 'OTROS_BANCOS') | currency}}</span>
        </span>
        <span layout>
          <span class="pad-left">Mismo: </span>
          <span class="pad-left">{{getTotal(fichas, 'MISMO_BANCO') | currency }}</span>
        </span>

      </span>

      <span flex></span>
      <sx-fichas-filter [filter]="filter$ | async" (aplicar)="onFilterChange($event)"></sx-fichas-filter>
      <span>
        <button mat-icon-button [matMenuTriggerFor]="toolbarMenu">
          <mat-icon>more_vert</mat-icon>
        </button>
        <mat-menu #toolbarMenu="matMenu">
          <button mat-menu-item  (click)="reload()"><mat-icon>refresh</mat-icon> Recargar</button>
          <a mat-menu-item  color="accent"  (click)="generar()">
            <mat-icon>perm_data_setting</mat-icon> Generar
          </a>
        </mat-menu>
      </span>
      </div>
      <mat-divider></mat-divider>

      <sx-fichas-table [fichas]="fichas$ | async"
        (edit)="onEdit($event)"
        (delete)="onDelete($event)"
        (ingreso)="onIngreso($event)"
        [filter]="search">
      </sx-fichas-table>
      <mat-card-actions>

      </mat-card-actions>
    </mat-card>

  `,
  styles: [
    `
      .mat-card {
        width: calc(100% - 15px);
        height: calc(100% - 10px);
      }
    `
  ]
})
export class FichasComponent implements OnInit {
  fichas$: Observable<Ficha[]>;
  search = '';
  filter$: Observable<FichaFilter>;

  constructor(
    private store: Store<fromStore.State>,
    private dialog: MatDialog,
    private reportService: ReportService,
    private dialogService: TdDialogService
  ) {}

  ngOnInit() {
    this.fichas$ = this.store.pipe(select(fromStore.getAllFichas));
    this.filter$ = this.store.pipe(select(fromStore.getFichasFilter));
  }

  onFilterChange(filter: FichaFilter) {
    this.store.dispatch(new fromStore.SetFichasFilter({ filter }));
  }

  reload() {
    this.store.dispatch(new fromStore.LoadFichas());
  }

  generar() {}

  onIngreso(event: Ficha) {}

  onEdit(event: Ficha) {}

  onDelete(event: Ficha) {}

  getTotal(fichas: Ficha[], tipo: string) {
    return _.sumBy(fichas, item => {
      if (item.tipoDeFicha === tipo) {
        return item.total;
      } else {
        return 0;
      }
    });
  }
}
