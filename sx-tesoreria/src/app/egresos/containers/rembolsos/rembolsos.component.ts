import { Component, OnInit } from '@angular/core';

import { Observable } from 'rxjs';

import { Store, select } from '@ngrx/store';
import * as fromRoot from 'app/store';
import * as fromStore from '../../store';

import { Rembolso, RembolsosFilter } from '../../models';

@Component({
  selector: 'sx-rembolsos',
  template: `
  <ng-template tdLoading [tdLoadingUntil]="!(loading$ | async)" tdLoadingStrategy="overlay">
    <mat-card >
      <sx-search-title title="Rembolsos a sucursales" (search)="search = $event">
        <sx-rembolsos-filter-btn [filter]="filter$ | async" class="options"
          (change)="onFilter($event)"></sx-rembolsos-filter-btn>
      </sx-search-title>
      <mat-divider></mat-divider>
      <div class="table-panel">
        <sx-rembolsos-table [rembolsos]="rembolsos$ | async" [filter]="search"
              (edit)="onEdit($event)">
        </sx-rembolsos-table>
      </div>
      <mat-card-footer>
        <sx-rembolsos-filter-label [filter]="filter$ | async"></sx-rembolsos-filter-label>
      </mat-card-footer>
    </mat-card>
  </ng-template>
  `,
  styles: [
    `
      .table-panel {
        min-height: 400px;
      }
      .table-det-panel {
        min-height: 200px;
      }
    `
  ]
})
export class RembolsosComponent implements OnInit {
  rembolsos$: Observable<Rembolso[]>;
  loading$: Observable<boolean>;
  filter$: Observable<RembolsosFilter>;
  search = '';

  constructor(private store: Store<fromStore.State>) {}

  ngOnInit() {
    this.rembolsos$ = this.store.pipe(select(fromStore.getAllRembolsos));
    this.loading$ = this.store.pipe(select(fromStore.getRembolsosLoading));
    this.filter$ = this.store.pipe(select(fromStore.getRembolsosFilter));
  }

  onFilter(filter: RembolsosFilter) {
    this.store.dispatch(new fromStore.SetRembolsosFilter({ filter }));
  }

  onEdit(event: Rembolso) {
    this.store.dispatch(
      new fromRoot.Go({ path: ['egresos/rembolsos', event.id] })
    );
  }
}
