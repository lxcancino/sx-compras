import { Component, OnInit, ChangeDetectionStrategy } from '@angular/core';

import { Store, select } from '@ngrx/store';
import * as fromStore from '../../store';
import * as fromCfdis from '../../store/actions/cfdi.actions';

import { Observable } from 'rxjs';
import { map } from 'rxjs/operators';
import { ComprobanteFiscalConcepto } from '../../model';

@Component({
  selector: 'sx-cfdis-conceptos',
  changeDetection: ChangeDetectionStrategy.OnPush,
  template: `
    <mat-card *ngIf="selection$ | async">
      <div layout layout-align="start center" class="pad-left-sm pad-right-sm">
        <span>Conceptos</span>
        <span flex></span>
        <button mat-icon-button (click)="clearSelection()">
          <mat-icon>close</mat-icon>
        </button>
      </div>
      <ng-container *ngIf="conceptos$ | async as conceptos">
        <sx-cfdis-conceptos-table [conceptos]="conceptos"></sx-cfdis-conceptos-table>
      </ng-container>
    </mat-card>
  `
})
export class CfdisConceptosComponent implements OnInit {
  selection$: Observable<boolean>;
  conceptos$: Observable<ComprobanteFiscalConcepto[]>;

  constructor(private store: Store<fromStore.State>) {}

  ngOnInit() {
    this.selection$ = this.store.pipe(
      select(fromStore.getComprobantesSelectedIds),
      map(ids => ids && ids.length > 0)
    );
    this.conceptos$ = this.store.pipe(select(fromStore.getSelectedConceptos));
  }

  clearSelection() {
    this.store.dispatch(new fromCfdis.SelectCfdis({ ids: [] }));
  }
}
